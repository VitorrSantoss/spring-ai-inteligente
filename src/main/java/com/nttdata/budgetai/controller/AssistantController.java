package com.nttdata.budgetai.controller;

import com.nttdata.budgetai.dto.AssistantResponse;
import com.nttdata.budgetai.dto.ChatRequest;
import com.nttdata.budgetai.service.AssistantService;
import com.nttdata.budgetai.service.SpeechService;
import com.nttdata.budgetai.service.TranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * AssistantController - Endpoint inteligente da API.
 *
 * Aqui é onde a mágica acontece. Os endpoints permitem ao usuário:
 *  1. Conversar por texto (POST /chat).
 *  2. Enviar áudio e receber texto (POST /voice/transcribe).
 *  3. Enviar áudio, processar com IA e receber áudio de volta
 *     (POST /voice/assist) → fluxo completo: Whisper → GPT+Tools → TTS.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Assistente inteligente com chat, transcrição e síntese de voz")
public class AssistantController {

    private final AssistantService assistantService;
    private final TranscriptionService transcriptionService;
    private final SpeechService speechService;

    // ---------- 1. Chat por texto ----------
    @PostMapping("/chat")
    @Operation(summary = "Conversa por texto com o assistente (executa tools)")
    public AssistantResponse chat(@RequestBody ChatRequest request) {
        String reply = assistantService.chat(request.message());
        return new AssistantResponse(request.message(), reply);
    }

    // ---------- 2. Transcrever áudio (Whisper) ----------
    @PostMapping(value = "/voice/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcreve um áudio para texto usando Whisper")
    public ResponseEntity<String> transcribe(@RequestParam("audio") MultipartFile audio) throws IOException {
        return ResponseEntity.ok(transcriptionService.transcribe(audio));
    }

    // ---------- 3. Sintetizar voz a partir de texto (TTS) ----------
    @PostMapping(value = "/voice/speak", produces = "audio/mpeg")
    @Operation(summary = "Converte texto em áudio MP3 (TTS)")
    public ResponseEntity<byte[]> speak(@RequestBody ChatRequest request) {
        byte[] audio = speechService.synthesize(request.message());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"reply.mp3\"");
        return ResponseEntity.ok().headers(headers).body(audio);
    }

    // ---------- 4. Fluxo completo: áudio → IA → áudio ----------
    @PostMapping(value = "/voice/assist",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = "audio/mpeg")
    @Operation(summary = "Fluxo completo: recebe áudio do usuário, processa com a IA (com tool calling) e devolve a resposta em áudio")
    public ResponseEntity<byte[]> voiceAssist(@RequestParam("audio") MultipartFile audio) throws IOException {
        // 1. Whisper transcreve a fala do usuário
        String userText = transcriptionService.transcribe(audio);

        // 2. ChatClient processa com tools (registra transação, consulta saldo, etc.)
        String reply = assistantService.chat(userText);

        // 3. TTS sintetiza a resposta em áudio
        byte[] replyAudio = speechService.synthesize(reply);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"assistant-reply.mp3\"");
        headers.add("X-User-Transcript", userText);
        headers.add("X-Assistant-Reply", reply);
        return ResponseEntity.ok().headers(headers).body(replyAudio);
    }

    // ---------- 5. Variante JSON (para debug ou frontend que prefere texto) ----------
    @PostMapping(value = "/voice/assist-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Mesma coisa do /voice/assist, mas devolve JSON com as duas strings (sem áudio)")
    public AssistantResponse voiceAssistJson(@RequestParam("audio") MultipartFile audio) throws IOException {
        String userText = transcriptionService.transcribe(audio);
        String reply = assistantService.chat(userText);
        return new AssistantResponse(userText, reply);
    }
}
