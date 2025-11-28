package com.vojtechruzicka.springaidemo;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


record Poem(String title, String content, String genre, String theme) {
}

@Service
public class PoetryService {
    private final ChatClient chatClient;
    private static final PromptTemplate TEMPLATE = new PromptTemplate(
            "Write a {genre} haiku about {theme} in 5-7-5 format.");

    public PoetryService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Poem generate(String genre, String theme) {
        Prompt prompt = TEMPLATE.create(Map.of("genre", genre, "theme", theme));
        return chatClient.prompt(prompt).call().entity(Poem.class);
    }
}

// Exposing a REST API

@RestController
public class PoemController {
    private final PoetryService service;

    public PoemController(PoetryService service) {
        this.service = service;
    }

    @PostMapping("/poems")
    public ResponseEntity<Poem> generate(@RequestBody PoemRequest req) {
        return ResponseEntity.ok(service.generate(req.genre(), req.theme()));
    }

    record PoemRequest(String genre, String theme) {
    }

}

    // Error Handling

@ExceptionHandler(OpenAiApiClientErrorException.class)
public ProblemDetail handleError(OpenAiApiClientErrorException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Unable to communicate with the configured LLM. Please try again later."
    );
}
