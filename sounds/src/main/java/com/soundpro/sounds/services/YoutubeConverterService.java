package com.soundpro.sounds.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.soundpro.sounds.dtos.LoadingFileDTO;
import com.soundpro.sounds.dtos.YoutubeConverterDTO;
import com.soundpro.sounds.utils.ConsumerUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class YoutubeConverterService extends ConsumerUtils{

    @Autowired
    private WebSocketService webSocketService;

    public MultipartFile convertYoutubeVideoUrlToMp3MultipartFile(YoutubeConverterDTO youtubeConverterDTO, LoadingFileDTO loadingFileDTO) {
        try {
            String comando1 = "/usr/bin/python3";
            String comando2 = "/usr/local/bin/youtube-dl";
            String pathPastaYoutubeVideosEFormatoNomeVideo = "~/youtube-videos/%(title)s.%(ext)s";
            String[] argumentos1 = { comando1, comando2, "--verbose", "--no-check-certificate","--format", "bestvideo+bestaudio",
                    "--merge-output-format", "mkv", "-o", pathPastaYoutubeVideosEFormatoNomeVideo, youtubeConverterDTO.getYoutubeVideoUrl() };
            String nomeVideoSemExtensao = executarComando(argumentos1, youtubeConverterDTO, webSocketService, loadingFileDTO);

            if (nomeVideoSemExtensao != null) {
                String parametroV = youtubeConverterDTO.getYoutubeVideoUrl().substring(youtubeConverterDTO.getYoutubeVideoUrl().indexOf("?v=") + 3);
                String nomeVideoSemExtensaoESemId = nomeVideoSemExtensao.replace("-" + parametroV, "");
                String comando3 = "/usr/bin/ffmpeg";
                String caminhoVideo = nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1) + ".mkv";
                System.out.println(caminhoVideo);
                String[] argumentos2 = { comando3, "-i", caminhoVideo, "-vn", "-c:a", "libmp3lame", "-b:a", "320k",
                        nomeVideoSemExtensaoESemId.substring(1, nomeVideoSemExtensaoESemId.length() - 1) + ".mp3" };
                executarComando(argumentos2, youtubeConverterDTO, webSocketService, loadingFileDTO);

                File arquivoVideo = new File(caminhoVideo);
                if (arquivoVideo.exists()) {
                    boolean deletado = arquivoVideo.delete();
                    if (deletado) {
                        log.info("Arquivo de vídeo "
                                + nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1)
                                + ".mkv deletado com sucesso.");
                    } else {
                        log.info("Falha ao deletar o arquivo de vídeo "
                                + nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1) + ".mkv");
                    }
                }
                String caminhoMp3 = nomeVideoSemExtensaoESemId.substring(1, nomeVideoSemExtensaoESemId.length() - 1)
                        + ".mp3";
                File arquivoMP3 = new File(caminhoMp3);
                if (arquivoMP3.exists()) {
                    MultipartFile multipartFileMp3 = this.convertToMultipartFile(arquivoMP3);
                    return multipartFileMp3;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    public int extractPercentage(String input) {
        Pattern patternPorcentagem = Pattern.compile("(\\d+\\.\\d+)%");  // Captura os números e o ponto, excluindo o %
        Matcher matcherPorcentagem = patternPorcentagem.matcher(input);

        if (matcherPorcentagem.find()) {
            double valorDouble = Double.parseDouble(matcherPorcentagem.group(1));
            return (int) Math.round(valorDouble);  // Converte o valor capturado para double e retorna
        }

        return -1;  // Se não encontrar porcentagem, retorna -1 (ou outro valor indicativo de erro)
    }

    public boolean containsMp4(String input) {
        return input.contains(".mp4");
    }

    public boolean containsMkvOrM4aOrWebm(String input) {
        return input.contains(".mkv") || input.contains(".m4a")|| input.contains(".webm");
    }


    public boolean existExtensionAndPercentage(String input) {
        // Verifica se possui extensão
        Pattern patternExtensao = Pattern.compile("\\.(mp4|mkv|m4a)");
        Matcher matcherExtensao = patternExtensao.matcher(input);
        boolean possuiExtensao = matcherExtensao.find();

        // Verifica se possui porcentagem
        Pattern patternPorcentagem = Pattern.compile("\\d+\\.\\d+%");  // Por exemplo: 0.0%, 100.0%, 12.5%
        Matcher matcherPorcentagem = patternPorcentagem.matcher(input);
        boolean possuiPorcentagem = matcherPorcentagem.find();

        return possuiExtensao && possuiPorcentagem;
    }

    private String executarComando(String[] comando, YoutubeConverterDTO youtubeConverterDTO, WebSocketService webSocketService, LoadingFileDTO loadingFileDTO) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(comando);
        processBuilder.redirectErrorStream(true);

        Process processo = processBuilder.start();

        // Cria um thread para ler a saída do processo
        LeitorOutput leitorOutput = new LeitorOutput(processo.getInputStream(), youtubeConverterDTO, webSocketService, loadingFileDTO, this);
        Thread threadLeitorOutput = new Thread(leitorOutput);
        threadLeitorOutput.start();

        // Aguarda o término do processo
        int resultado = processo.waitFor();

        // Verifica se o processo terminou com sucesso
        if (resultado == 0) {
            log.info("Comando executado com sucesso.");
        } else {
            log.info("Ocorreu um erro ao executar o comando.");
        }

        // Obtém a saída do processo da classe LeitorOutput
        String saidaProcesso = leitorOutput.getSaida();

        // Retorna o nome do arquivo de vídeo baixado
        return extrairNomeArquivoBaixado(saidaProcesso, resultado == 0);
    }

    private String extrairNomeArquivoBaixado(String saidaProcesso, boolean processoExecutadoComSucesso) {
        if (processoExecutadoComSucesso) {
            String[] linhas = saidaProcesso.split("\n");
            for (String linha : linhas) {
                if (linha.startsWith("[ffmpeg] Merging formats into")) {
                    // Extrai o nome do arquivo de vídeo a partir da linha
                    String nomeComExtensao = linha.substring("[ffmpeg] Merging formats into".length()).trim();
                    String nomeSemExtensao = nomeComExtensao.replaceAll(".mkv", "");
                    return nomeSemExtensao;
                }
            }
        }
        return null;
    }

    private static class LeitorOutput implements Runnable {
        private final InputStream inputStream;
        private final StringBuilder saida;
        private final YoutubeConverterDTO youtubeConverterDTO;
        private final WebSocketService webSocketService;
        private final LoadingFileDTO loadingFileDTO;
        private final YoutubeConverterService youtubeConverterService;
        

        public LeitorOutput(InputStream inputStream, YoutubeConverterDTO youtubeConverterDTO, WebSocketService webSocketService, LoadingFileDTO loadingFileDTO, YoutubeConverterService youtubeConverterService) {
            this.inputStream = inputStream;
            this.saida = new StringBuilder();
            this.youtubeConverterDTO = youtubeConverterDTO;
            this.webSocketService = webSocketService;
            this.loadingFileDTO = loadingFileDTO;
            this.youtubeConverterService = youtubeConverterService;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String linha;
                String nomeVideo = "";
                int progressPercentage = 10;
                int nomeVideoWebSocketCount = 1;
                int progressElevateMp4 = 10;
                int progressElevateMkvOrM4aOrWebm = 10;
                while ((linha = reader.readLine()) != null) {
                    if (linha.startsWith("[download] Destination:")) {
                        String nomeVideoCompleto = linha.substring("[download] Destination:".length()).trim();
                        String replaceAll = System.getProperty("user.home") + "/youtube-videos/";
                        String regex = Pattern.quote(replaceAll);
                        nomeVideo = nomeVideoCompleto.replaceAll(regex, "");
                        if(nomeVideoWebSocketCount > 0){
                            this.loadingFileDTO.setSoundName(nomeVideo.substring(0, nomeVideo.length() - 9));
                            this.loadingFileDTO.setProgressPercentage(10);
                            this.webSocketService.sendMessage("/topic/progress/" + this.youtubeConverterDTO.getRequestId(), this.youtubeConverterService.jsonMapper().toJson(loadingFileDTO));
                            nomeVideoWebSocketCount = 0;
                        }
                    }
                    if (linha.startsWith("[download]") && !linha.startsWith("[download] Destination:")) {
                        log.info(nomeVideo + " - " + linha);
                        if(this.youtubeConverterService.existExtensionAndPercentage(nomeVideo + " - " + linha)){
                            System.out.println("Entrou no extract extenions and percentage");
                            int progressYoutubeConverter = this.youtubeConverterService.extractPercentage(nomeVideo + " - " + linha);
                            System.out.println(progressYoutubeConverter);
                            if(this.youtubeConverterService.containsMp4(nomeVideo + " - " + linha)){
                                System.out.println("Entrou no contains mp4 or m4a");
                                if(progressYoutubeConverter >= progressElevateMp4){
                                    System.out.println("Entrou no progress converter do mp4 ou m4a");
                                    if(progressYoutubeConverter - progressElevateMp4 < 10){
                                        System.out.println("Entrou no progress menor que mp4");
                                        progressPercentage += 3;
                                        System.out.println(progressPercentage);
                                        progressElevateMp4 += 10;
                                    } else {
                                        System.out.println("Entrou no else do progress menor que mp4");
                                        int progressCalc = progressYoutubeConverter / 10 * 3;
                                        System.err.println("progress calc " + progressCalc);
                                        progressPercentage = progressPercentage + progressCalc;
                                        System.out.println("Progress - " + progressPercentage);
                                        int unidades = progressYoutubeConverter % 10;
                                        if (unidades >= 5) {
                                            progressElevateMp4 = progressYoutubeConverter + (10 - unidades);  // Arredonda para cima
                                        } else {
                                            progressElevateMp4 = progressYoutubeConverter - unidades;  // Arredonda para baixo
                                        }
                                    }
                                    this.loadingFileDTO.setProgressPercentage(progressPercentage);
                                    this.webSocketService.sendMessage("/topic/progress/" + this.youtubeConverterDTO.getRequestId(), this.youtubeConverterService.jsonMapper().toJson(loadingFileDTO));
                                }
                            }
                            if(this.youtubeConverterService.containsMkvOrM4aOrWebm(nomeVideo + " - " + linha)){
                                System.out.println("Entrou no contains do mkv");
                                if(progressYoutubeConverter >= progressElevateMkvOrM4aOrWebm){
                                    System.out.println("Entrou no progress converter do mkv");
                                    if(progressYoutubeConverter - progressElevateMkvOrM4aOrWebm < 10){
                                        System.out.println("Entrou no progress menor que mkv or m4a");
                                        progressPercentage += 3;
                                        System.out.println(progressPercentage);
                                        progressElevateMkvOrM4aOrWebm += 10;
                                    } else {
                                        System.out.println("Entrou no else do progress menor que mkv or m4a");
                                        int progressCalc = progressYoutubeConverter / 10 * 3;
                                        System.err.println("progress calc " + progressCalc);
                                        progressPercentage = progressPercentage + progressCalc;
                                        System.out.println("Progress - " + progressPercentage);
                                        int unidades = progressYoutubeConverter % 10;
                                        if (unidades >= 5) {
                                            progressElevateMkvOrM4aOrWebm = progressYoutubeConverter + (10 - unidades);  // Arredonda para cima
                                        } else {
                                            progressElevateMkvOrM4aOrWebm = progressYoutubeConverter - unidades;  // Arredonda para baixo
                                        }
                                    }
                                    this.loadingFileDTO.setProgressPercentage(progressPercentage);
                                    this.webSocketService.sendMessage("/topic/progress/" + this.youtubeConverterDTO.getRequestId(), this.youtubeConverterService.jsonMapper().toJson(loadingFileDTO));
                                }
                            }
                        }
                        saida.append(linha).append("\n");
                    } else {
                        log.info(linha);
                        saida.append(linha).append("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getSaida() {
            return saida.toString();
        }
    }

    private MultipartFile convertToMultipartFile(File file) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                try (InputStream inputStream = new FileInputStream(file)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return outputStream.toByteArray();
                }
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (InputStream inputStream = new FileInputStream(file);
                        OutputStream outputStream = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        };
    }

}