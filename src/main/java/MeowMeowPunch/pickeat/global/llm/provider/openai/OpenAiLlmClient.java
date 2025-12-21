package MeowMeowPunch.pickeat.global.llm.provider.openai;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import MeowMeowPunch.pickeat.global.llm.config.LlmProperties;
import MeowMeowPunch.pickeat.global.llm.dto.LlmClient;
import MeowMeowPunch.pickeat.global.llm.dto.LlmRequest;
import MeowMeowPunch.pickeat.global.llm.dto.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.exception.LlmException;
import MeowMeowPunch.pickeat.global.llm.exception.LlmUpstreamException;
import reactor.core.publisher.Mono;

/**
 * [Global][LLM][OpenAI] OpenAI 구현체.
 *
 * <pre>
 * [LlmClient] ──▶ [OpenAiLlmClient] ──▶ (WebClient) ──▶ OpenAI API
 * </pre>
 *
 * - OpenAI Chat Completion API 호출
 * - 비동기/동기 처리 지원
 */
// OpenAI Chat Completions 호출 구현
public class OpenAiLlmClient implements LlmClient {

	private final WebClient webClient;
	private final LlmProperties props;

	public OpenAiLlmClient(WebClient webClient, LlmProperties props) {
		this.webClient = webClient;
		this.props = props;
	}

@Override
	public LlmResponse generate(LlmRequest request) {
		if (props.openai() == null) {
			throw new LlmException("llm.openai 설정이 없습니다. api-key/model/base-url을 확인하세요.");
		}
		if (props.openai().apiKey() == null || props.openai().model() == null) {
			throw new LlmException("llm.openai.api-key 또는 model 값이 비어 있습니다.");
		}

		String baseUrl = props.openai().baseUrl();
		String model = props.openai().model();

		ChatRequest body = OpenAiRequestMapper.toChatRequest(model, request);

		try {
			ChatResponse res = webClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.openai().apiKey())
				.bodyValue(body)
				.exchangeToMono(resp -> handleResponse(resp, baseUrl, model))
				.timeout(Duration.ofMillis(props.timeoutMs() > 0 ? props.timeoutMs() : 30000))
				.block();

			return OpenAiResponseMapper.toLlmResponse(res);

		} catch (LlmException e) {
			throw e;
		} catch (Exception e) {
			throw OpenAiErrorMapper.map(e);
		}
	}

	private Mono<ChatResponse> handleResponse(ClientResponse resp, String baseUrl, String model) {
		if (resp.statusCode().is2xxSuccessful()) {
			return resp.bodyToMono(ChatResponse.class);
		}

		// 핵심: 에러 바디 전문을 문자열로 확보
		return resp.bodyToMono(String.class)
			.defaultIfEmpty("")
			.flatMap(body -> {
				// body를 포함한 예외로 터뜨려 상위에서 fallback/매핑하도록
				return Mono.error(new LlmUpstreamException("OpenAI error: HTTP "
					+ resp.statusCode().value() + " body=" + body, null));
			});
	}
}
