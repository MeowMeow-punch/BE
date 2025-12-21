package MeowMeowPunch.pickeat.global.llm.provider.openai;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import MeowMeowPunch.pickeat.global.llm.config.LlmProperties;
import MeowMeowPunch.pickeat.global.llm.core.LlmClient;
import MeowMeowPunch.pickeat.global.llm.core.LlmRequest;
import MeowMeowPunch.pickeat.global.llm.core.LlmResponse;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

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
		ChatRequest body = OpenAiRequestMapper.toChatRequest(props.openai().model(), request);

		try {
			ChatResponse res = webClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.openai().apiKey())
				.bodyValue(body)
				.retrieve()
				.bodyToMono(ChatResponse.class)
				.timeout(Duration.ofMillis(props.timeoutMs() > 0 ? props.timeoutMs() : 30000))
				.block();

			return OpenAiResponseMapper.toLlmResponse(res);
		} catch (Exception e) {
			throw OpenAiErrorMapper.map(e);
		}
	}
}
