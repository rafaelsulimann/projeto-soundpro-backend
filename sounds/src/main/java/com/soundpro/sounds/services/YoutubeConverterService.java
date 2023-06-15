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

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class YoutubeConverterService {

    public MultipartFile convertYoutubeVideoUrlToMp3MultipartFile(String youtubeVideoUrl) {
        try {
            String comando1 = "/usr/bin/python3.8";
            String comando2 = "/usr/local/bin/youtube-dlc";
            String[] argumentos1 = { comando1, comando2, "--verbose", "--format", "bestvideo+bestaudio", "--merge-output-format", "mkv", youtubeVideoUrl };
            String nomeVideoSemExtensao = executarComando(argumentos1);

            if (nomeVideoSemExtensao != null) {
                String parametroV = youtubeVideoUrl.substring(youtubeVideoUrl.indexOf("?v=") + 3);
                String nomeVideoSemExtensaoESemId = nomeVideoSemExtensao.replace("-" + parametroV, "");
                String comando3 = "/usr/bin/ffmpeg";
                String caminhoVideo = System.getProperty("user.dir") + "/" + nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1) + ".mkv";
                String[] argumentos2 = { comando3, "-i", caminhoVideo, "-vn", "-c:a", "libmp3lame", "-b:a", "320k", nomeVideoSemExtensaoESemId.substring(1, nomeVideoSemExtensaoESemId.length() - 1) + ".mp3" };
                executarComando(argumentos2);

                File arquivoVideo = new File(caminhoVideo);
                if (arquivoVideo.exists()) {
                    boolean deletado = arquivoVideo.delete();
                    if (deletado) {
                        System.out.println("Arquivo de vídeo " + nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1) + ".mkv deletado com sucesso.");
                    } else {
                        System.out.println("Falha ao deletar o arquivo de vídeo " + nomeVideoSemExtensao.substring(1, nomeVideoSemExtensao.length() - 1) + ".mkv");
                    }
                }
                String caminhoMp3 = System.getProperty("user.dir") + "/" + nomeVideoSemExtensaoESemId.substring(1, nomeVideoSemExtensaoESemId.length() - 1) + ".mp3";
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

    private String executarComando(String[] comando) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(comando);
        processBuilder.redirectErrorStream(true);

        Process processo = processBuilder.start();

        // Cria um thread para ler a saída do processo
        LeitorOutput leitorOutput = new LeitorOutput(processo.getInputStream());
        Thread threadLeitorOutput = new Thread(leitorOutput);
        threadLeitorOutput.start();

        // Aguarda o término do processo
        int resultado = processo.waitFor();

        // Verifica se o processo terminou com sucesso
        if (resultado == 0) {
            System.out.println("Comando executado com sucesso.");
        } else {
            System.out.println("Ocorreu um erro ao executar o comando.");
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

        public LeitorOutput(InputStream inputStream) {
            this.inputStream = inputStream;
            this.saida = new StringBuilder();
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println(linha);
                    saida.append(linha).append("\n");
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
