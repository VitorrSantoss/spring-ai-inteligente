package com.nttdata.budgetai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// ⚠️ Pacote correto na 1.0 GA: org.springframework.ai.audio.transcription
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * TranscriptionService - Transforma áudio em texto usando OpenAI Whisper.
 * Suporta mp3, mp4, mpeg, mpga, m4a, wav, webm (limite de 25 MB).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public String transcribe(MultipartFile audioFile) throws IOException {
        log.info("🎙️ Transcrevendo áudio: {} ({} bytes)",
                audioFile.getOriginalFilename(), audioFile.getSize());

        // O Whisper precisa de um Resource com filename (ele infere o formato pela extensão)
        Resource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename() != null
                        ? audioFile.getOriginalFilename()
                        : "audio.mp3";
            }
        };

        // Na 1.0 GA o builder usa métodos sem o prefixo "with"
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language("pt")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .temperature(0f)
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);
        String transcript = transcriptionModel.call(prompt).getResult().getOutput();
        log.info("📝 Transcrição: {}", transcript);
        return transcript;
    }
}
