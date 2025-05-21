package org.xiaoyu.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Slf4j
@RestController
public class ChatClientExample {

    private final ChatClient chatClient;

    @Autowired
    ImageModel openaiImageModel;

    @Autowired
    OpenAiAudioTranscriptionModel openaiAudioTranscriptionModel;

    @Autowired
    OpenAiAudioSpeechModel  openaiSpeechModel;


    public ChatClientExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/gen_image")
    public List<String> genImage() {
        ImageResponse response = openaiImageModel.call(
                new ImagePrompt("一个可以奔跑的小雨滴",
                        OpenAiImageOptions.builder()
                                .height(1024)
                                .width(1024).build())

        );
        List<String> urls = response.getResults().stream().map(imageGeneration -> imageGeneration.getOutput().getUrl()).toList();
        return urls;
    }

    @PostMapping("/chat_image")
    public String chatImage() {
        var imageResource = new ClassPathResource("/1.jpg");

        var userMessage = new UserMessage("解释下你从这张图片看到什么了?",
                new Media(MimeTypeUtils.IMAGE_JPEG, imageResource));

        String content = chatClient.prompt(new Prompt(userMessage,
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build())).call().content();
        return content;
    }

    @PostMapping("/chat_image_url")
    public String chatImageUrl() throws MalformedURLException {
        var userMessage = new UserMessage("解释下你从这张图片看到什么了?",
                Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_PNG)
                        .data(new URL(
                                "https://cloudcache.tencent-cloud.com/qcloud/ui/portal-set/build/About/images/bg-product-series_87d.png"))
                        .build());

        String content = chatClient.prompt(new Prompt(userMessage,
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build())).call().content();
        return content;
    }

    @PostMapping("/chat_audio")
    public String chatAduio() {
        var userMessage = new UserMessage("这段音乐说啥了",
                Media.builder()
                        .mimeType(MimeTypeUtils.parseMimeType("audio/mp3"))
                        .data(new ClassPathResource("2.mp3"))
                        .build());

        String content = chatClient.prompt(new Prompt(userMessage,
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW.getValue()).build())).call().content();
        return content;
    }

    @PostMapping("/chat_audio_url")
    public ResponseEntity<byte[]> chatAduioUrl() throws MalformedURLException {
        var userMessage = new UserMessage("给我讲个笑话吧");

        ChatResponse response = chatClient.prompt(new Prompt(List.of(userMessage),
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW)
                        .outputModalities(List.of("text", "audio"))
                        .outputAudio(new OpenAiApi.ChatCompletionRequest.AudioParameters(OpenAiApi.ChatCompletionRequest.AudioParameters.Voice.ALLOY, OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat.WAV))
                        .build())).call().chatResponse();

        String text = response.getResult().getOutput().getText(); // audio transcript
        byte[] waveAudio = response.getResult().getOutput().getMedia().get(0).getDataAsByteArray(); // audio data
        // 构建文件下载响应
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audio.wav")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(waveAudio);
    }

    @PostMapping("/transcription_audio2text")
    public String transcriptionAudio2Text() throws MalformedURLException {

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .language("zh")
                .temperature(0f)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();
//        var audioFile = new UrlResource(new URL("https://output.lemonfox.ai/wikipedia_ai.mp3"));
        var audioFile = new ClassPathResource("2.mp3");
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile,transcriptionOptions);
        AudioTranscriptionResponse response = openaiAudioTranscriptionModel.call(transcriptionRequest);
        return response.getResults().get(0).getOutput();
    }
    @PostMapping("/speech_text2audio")
    public ResponseEntity<byte[]> speechText2Audio() {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt("你好，我是努力的小雨", speechOptions);
        SpeechResponse response = openaiSpeechModel.call(speechPrompt);
        byte[] audioBytes = response.getResult().getOutput();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audio.wav")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audioBytes);
    }
}
