package br.ufs.dcomp.ChatRabbitMQ;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.rabbitmq.client.Channel;

public class SendFile extends Thread
{
    private String filePath; // Caminho para o arquivo
    private String sender; // Rota para envio da mensagem, pode ser um exchange ou uma fila
    private String queueName; // Nome da fila, o emissor
    private Channel channel; // Variável com o canal para o RAbbitMQ
    private ProtocolBuffers message; // Variável com a mensagem, em ProtocolBuffers, a ser enviada
    
    public SendFile(String filePath, String sender, String queueName, Channel channel, ProtocolBuffers message)
    {
        this.filePath = filePath;
        this.sender = sender;
        this.queueName = queueName;
        this.channel = channel;
        this.message = message;
    }
    
    public void run()
    {
        try{
            File file = new File(this.filePath); // Obtém o arquivo
            byte[] fileBytes = Files.readAllBytes(file.toPath()); // Recupera os bytes do arquivo
            String[] subdirs = this.filePath.split("/"); // Obtém os subdiretórios
            String filename = subdirs[subdirs.length - 1]; // Obtém o nome do arquivo, com a extensão
            String mimeType = Files.probeContentType(Paths.get(filename)); // Obtém o tipo MIME do arquivo
            
            // Verifica se o arquivo a ser enviado é para um grupo
            if(this.sender.charAt(0) == '#'){
                this.sender = this.sender.replace("#", ""); // Remove o caractere '#'
                
                // Serializa a mensagem com o ProtocolBuffers para enviar a um grupo
                PadraoMensagem.Mensagem messageProtocolBuffers = this.message.createMessage(fileBytes, this.queueName, this.sender, mimeType, filename);
                
                // Publica a mensagem (arquivo) no exchange para envio de arquivos
                this.channel.basicPublish(this.sender + "Files", "files", null, messageProtocolBuffers.toByteArray());
                
                String warning = "Arquivo " + '"' + this.filePath + '"' + " foi eviado para #" + this.sender + "!";

                messageProtocolBuffers = this.message.createMessage(warning.getBytes(), "Sistema", "", "text/plain", "");
                
                this.channel.basicPublish("", this.queueName, null, messageProtocolBuffers.toByteArray());
            } else{
                this.sender = this.sender.replace("@", ""); // Remove o caractere '@'

                // Serializa a mensagem com o ProtocolBuffers para enviar a um usuário
                PadraoMensagem.Mensagem messageProtocolBuffers = this.message.createMessage(fileBytes, this.queueName, "", mimeType, filename);

                // Publica a mensagem (arquivo) na fila de arquivo do usuário
                this.channel.basicPublish("", this.sender + "Files", null, messageProtocolBuffers.toByteArray());

                String warning = "Arquivo " + '"' + this.filePath + '"' + " foi eviado para @" + this.sender + "!";

                messageProtocolBuffers = this.message.createMessage(warning.getBytes(), "Sistema", "", "text/plain", "");
                
                this.channel.basicPublish("", this.queueName, null, messageProtocolBuffers.toByteArray());
            }
        } catch(Exception e){
            System.out.println("\nGrupo ou usuário não encontrado\n");
        }
    }
}