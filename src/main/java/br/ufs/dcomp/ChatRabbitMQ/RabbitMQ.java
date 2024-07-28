package br.ufs.dcomp.ChatRabbitMQ;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.JsonNode;


public class RabbitMQ
{
    private String user; // Variável para armazenar o usuário do rabbitmq
    private String password; // Variável para armazenar a senha do rabbitmq
    private String queueName; // Variável para armazenar o nome da fila do usuário principal
    private String currentUser; // Variável para armazenar o nome do usuário atual de conversação
    private Channel fileChannel; // Variável para criar um canal de comunicação com o RAbbitMQ
    private Integer maxMessages; // Variável para informar quantas mensagens podem ser exibidas na tela
    private Connection connection; // Variável para representar uma conexão com o RabbitMQ
    private Consumer consumerFiles; // Variável para consumir arquivos de uma fila
    private Channel messageChannel; // Variável para criar um canal de comunicação com o RAbbitMQ
    private ProtocolBuffers message; // Variável para manipulação do ProtocolBuffers
    private Consumer consumerMessage; // Variável para consumir mensagens de uma fila
    private ConnectionFactory factory; // Variável para realizar as configurações iniciais do RabbitMQ
    private LinkedList<PadraoMensagem.Mensagem> messageBuffer; // Variável para armazenar as mensagens recebidas

    public RabbitMQ(String userName, Integer maxMessages, String user, String password) throws Exception
    {
        this.factory = new ConnectionFactory(); // Instancia uma classe de configuração
        this.factory.setHost("RabbitMQBalancer-fcd77e6581d8de95.elb.us-east-1.amazonaws.com"); // Insere o domínio do servidor do RabbitMQ
        this.factory.setUsername(user); // Insere o nome do login com o servidor
        this.factory.setPassword(password); // Insere a senha para login do servidor
        this.factory.setVirtualHost("teste"); // Define a "pasta" inicial dos canais
        this.user = user; // Armazena o usuário
        this.password = password; // Armazena a senha
        this.connection = this.factory.newConnection(); // Estabelece uma conexão com o RabbitMQ
        this.fileChannel = this.connection.createChannel(); // Estabelece um novo canal de comunicação com o RabbitMQ para arquivos
        this.fileChannel.basicQos(0);
        this.messageChannel = this.connection.createChannel(); // Estabelece um novo canal de comunicação com o RabbitMQ para mensagens
        this.messageChannel.basicQos(2);
        this.queueName = userName; // Define o nome da fila do usuário principal
        this.currentUser = ""; // Define como "nulo" o usuário atual de conversação
        this.createQueue(userName); // Cria uma nova fila no RabbitMQ com o nome do usuário principal
        this.messageBuffer = new LinkedList<PadraoMensagem.Mensagem>(); // Instanciando uma nova fila de mensagens recebidas
        this.maxMessages = maxMessages; // Atribuindo a quantidade máxima de mensagens recebidas
        this.message = new ProtocolBuffers(userName); // Instanciando um novo gerenciador de mensagens para ProtocolBuffers
    }

    public void printMessage() throws Exception
    {
        new Functools().clear();

        // Verifica se a quantidade de mensagens da fila é maior do que a quantidade desejada, removendo caso ultrapasse
        if(this.messageBuffer.size() > this.maxMessages) this.messageBuffer.pop();
        
        // Iteração na fila de mensagens para exibição
        for(int i = 0; i < this.messageBuffer.size(); i++)
        {
            if(i < this.maxMessages) // Verifica se já atingiu o limite de mensagens que podem ser exibidas
            {
                String date = this.messageBuffer.get(i).getData(); // Obtém a data de envio da mensagem
                String hour = this.messageBuffer.get(i).getHora(); // Obtém o horário de envio da mensagem
                String group = this.messageBuffer.get(i).getGrupo(); // Obtém o grupo para envio da mensagem
                String sender = this.messageBuffer.get(i).getEmissor(); // Obtém o emissor da mensagem
                String message = this.messageBuffer.get(i).getConteudo().getCorpo().toStringUtf8(); // Obtém a mensagem, já convertida em string

                // Verificação para inserir o tipo de impressão da mensagem
                if(group.equalsIgnoreCase("")) System.out.println("(" + date + " às " + hour + ") " + sender + " diz: " + message);
                else System.out.println("(" + date + " às " + hour + ") " + sender + "#" + group + " diz: " + message);
            } else break; // Encerra o loop
        }
        // Verificação para inserir o tipo de impressão no console
        if(!this.currentUser.equalsIgnoreCase("")) System.out.print("\n" + this.currentUser + ">> ");
        else System.out.print("\n>> ");
    }

    public void setCurrentUser(String user)
    {
        this.currentUser = user; // Define o nome do usuário atual de conversação
    }
    
    public String getCurrentUser()
    {
        return this.currentUser; // Obtém o nome do usuário atual de conversação
    }
    
    public void endRabbitMQ() throws Exception
    {
        this.messageChannel.close(); // Encerra a comunicação do canal de mensagens com o RabbitMQ
        this.fileChannel.close(); // Encerra a comunicação do canal de arquivos com o RabbitMQ
        this.connection.close(); // Encerra a conexão com o RabbitMQ
    }

    public void createQueue(String queueName) throws Exception
    {
        this.messageChannel.queueDeclare(queueName, false, false, false, null); // Cria uma fila no RabbitMQ com base no argumento passado
        this.fileChannel.queueDeclare(queueName + "Files", false, false, false, null); // Cria uma fila para arquivos
    }
    
    public void createExchange(String exchangeName) throws Exception
    {
        this.messageChannel.exchangeDeclare(exchangeName, "fanout"); // Cria um exchange do tipo FANOUT, que significa a criação de um grupo
        this.fileChannel.exchangeDeclare(exchangeName + "Files", "fanout"); // Cria um exchange, apenas para arquivos, do tipo FANOUT
    }
    
    public void sendMessage(byte[] message, String routingKey) throws Exception
    {
        try{
            if(routingKey.charAt(0) == '#'){ // Verifica se o identificador atual pertence à um grupo
                routingKey = routingKey.replace("#", ""); // Remove o caractere '#'
                PadraoMensagem.Mensagem bmessage = this.message.createMessage(message, this.queueName, routingKey, "text/plain", ""); // Cria uma mensagem no padrão ProtocolBuffers
                this.messageChannel.basicPublish(routingKey, "group", null, bmessage.toByteArray()); // Envia a mensagem para o exchange pertencente ao grupo
            } else{
                routingKey = routingKey.replace("@", ""); // Remove o caractere '@'
                PadraoMensagem.Mensagem bmessage = this.message.createMessage(message, "Você", "", "text/plain", ""); // Cria uma mensagem no padrão ProtocolBuffers
                this.messageChannel.basicPublish("", routingKey, null, bmessage.toByteArray()); // Envia a mensagem para a fila do usuário de conversação atual
                this.messageChannel.basicPublish("", this.queueName, null, bmessage.toByteArray()); // Envia a mensagem para a fila do usuário de conversação atual
            }
        }catch(Exception e){
            System.out.print("\nGrupo ou usuário não encontrado\n");
        }
    }
 
    public void sendFiles(String filePath, String routingKey) throws Exception
    {
        // Cria uma Thread para enviar o arquivo
        SendFile send = new SendFile(filePath, routingKey, this.queueName, this.fileChannel, this.message);
        send.start(); // Inicia a Thread
        String warning = "Enviando " + '"' + filePath + '"' + " para " + routingKey;
        PadraoMensagem.Mensagem bmessage = this.message.createMessage(warning.getBytes(), "Sistema", "", "text/plain", "");
        this.messageChannel.basicPublish("", this.queueName, null, bmessage.toByteArray());
    }

    public void receiveMessage() throws Exception
    {
        this.consumerMessage = new DefaultConsumer(this.messageChannel)
        {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
            {
                PadraoMensagem.Mensagem mensagem = PadraoMensagem.Mensagem.parseFrom(body); // Converte a mensagem para o tipo ProtocolBuffers
                
                messageBuffer.add(mensagem); // Adiciona a mensagem na fila
                
                try{
                    printMessage(); // Imprime as mensagens da fila
                } catch(Exception e)
                {
                    System.out.print("Erro ao receber mensagem");
                }
            }
        };

        this.messageChannel.basicConsume(this.queueName, true, this.consumerMessage); // Cria uma Thread para acionar a função "consumer" quando uma nova mensagem estiver disponível na fila
    }

    public void receiveFiles() throws Exception
    {
        this.consumerFiles = new DefaultConsumer(this.fileChannel)
        {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
            {
                PadraoMensagem.Mensagem mensagem = PadraoMensagem.Mensagem.parseFrom(body);
                
                String date = mensagem.getData(); // Obtém a data de envio da mensagem
                String hour = mensagem.getHora(); // Obtém o horário de envio da mensagem
                String sender = mensagem.getEmissor(); // Obtém o emissor da mensagem
                String name = mensagem.getConteudo().getNome(); // Obtém o nome do arquivo

                if(!sender.equalsIgnoreCase(queueName)){
                    try
                    {
                        saveFile(mensagem);
                        System.out.println("\n(" + date + " às " + hour + ") Arquivo " + '"' + name + '"' + " recebido de @" + sender + '!');
                    } catch(Exception e)
                    {
                        System.out.println("Ocorreu um erro: " + e);
                    }
                }
            }
        };

        this.fileChannel.basicConsume(this.queueName + "Files", true, this.consumerFiles);
    }
    
    public void addUser(String exchange, String user) throws Exception
    {
        this.messageChannel.queueBind(user, exchange, "group"); // Vincula a fila do usuário em 'user' ao exchange
        this.fileChannel.queueBind(user + "Files", exchange + "Files", "files"); // Vincula a fila do usuário em 'user' ao exchange de arquivos
    }

    public void listUsers(String exchange) throws Exception
    {
        JsonNode json = new RabbitHTTP(this.user, this.password).responseAPI(exchange);

        System.out.print("\nUsuários em " + '"' + exchange + '"' + ": ");

        for(int i = 0; i < json.size(); i++)
        {
            String name = json.get(i).get("destination").asText();
            if(i == (json.size() - 1)) System.out.println(name + "\n");
            else System.out.print(name + ", ");
        }
    }

    public void removeUser(String exchange, String user) throws Exception
    {
        this.messageChannel.queueUnbind(user, exchange, "group"); // Desvincula o usuário em 'user' do exchange
        this.fileChannel.queueUnbind(user + "Files", exchange + "Files", "files"); // Desvincula o usuário em 'user' do exchange de arquivos
    }

    public void deleteExchange(String exchange) throws Exception
    {
        this.messageChannel.exchangeDelete(exchange); // Exclui o exchange, que significa excluir o grupo
        this.fileChannel.exchangeDelete(exchange + "Files"); // Exclui o exchange de arquivos
    }

    public void saveFile(PadraoMensagem.Mensagem message) throws Exception
    {
        String downloadPath = "./downloads/"; // Obtém o local para salvar o arquivo
        byte[] bytesFile = message.getConteudo().getCorpo().toByteArray(); // Recupera os bytes do arquivo
        String filename = message.getConteudo().getNome(); // Obtém o nome do arquivo
        FileOutputStream file = new FileOutputStream(downloadPath + filename); // Cria o arquivo
        file.write(bytesFile); // Insere os bytes no arquivo
        file.close(); // Fecha o arquivo
    }

}