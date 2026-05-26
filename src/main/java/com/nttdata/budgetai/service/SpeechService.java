package com.nttdata.budgetai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.stereotype.Service;

/**
 * SpeechService - Converte texto em áudio (Text-to-Speech) com OpenAI TTS.
 * Vozes disponíveis: alloy, echo, fable, onyx, nova, shimmer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeechService {

    private final OpenAiAudioSpeechModel speechModel;

    public byte[] synthesize(String text) {
        log.info("🔊 Sintetizando voz para: \"{}...\"",
                text.length() > 60 ? text.substring(0, 60) : text);

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model(OpenAiAudioApi.TtsModel.TTS_1.getValue())
                .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .build();

        SpeechPrompt prompt = new SpeechPrompt(text, options);
        return speechModel.call(prompt).getResult().getOutput();
    }
}
