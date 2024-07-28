package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;


public class Chat {
    
    public static void main(String[] argv)  throws Exception{
        Functools functions = new Functools(); // Instanciando classe de funções adicionais
        Scanner entry = new Scanner(System.in); // Instanciando classe para obtenção de entrada do teclado
        
        functions.clear(); // Limpando o terminal
        
        System.out.print("User: ");
        
        String userName = entry.nextLine(); // Obtendo nome do usuário pela entrada do teclado
        
        Integer maxMessages = 5; // Variável para armazenar o máximo de mensagens que podem ser exibidas na tela
        
        RabbitMQ rabbitmq = new RabbitMQ(userName, maxMessages, "admin", "09jlmn0950"); // Instanciando classe para manuseio do RabbitMQ

        functions.clear(); // Limpando terminal
        
        functions.sleep(100); // Esperando 100ms antes da verificação de recebimento de mensagens
        
        rabbitmq.receiveMessage(); // Verifica mensagens recebidas
        rabbitmq.receiveFiles(); // Verifica arquivos recebidos

        functions.sleep(1000); // Espera 1s antes da impressão e leitura da entrada

        System.out.print("\n>> ");
        
        String target = ""; // Variável para armazenar o usuário de conversação atual ou o grupo atual
        
        boolean exit = false; // Variável para identificar se o usuário deseja ou não encerrar o programa
        
        // Loop para inserir os usuários com quem se deseja iniciar um chat
        while(true){
            String message = entry.nextLine(); // Obtém o usuário alvo por meio da entrada do teclado
            
            // Switch para identificar os comandos ou envio de mensagens
            switch(message.charAt(0)){
                case '@':
                    functions.clear(); // Limpando o terminal
                    target = message; // Recupera o usuário atual
                    rabbitmq.setCurrentUser(target); // Define o usuário atual no objeto RabbitMQ
                    break;
                case '#':
                    functions.clear(); // Limpando o terminal
                    target = message; // Recupera o grupo atual
                    rabbitmq.setCurrentUser(target); // Define o grupo atual no objeto RabbitMQ
                    break;
                case '!':
                    message = message.replace("!", ""); // Remove o caractere '!' de comando
                    String[] command = message.split(" "); // Quebra a mensagem em partes individuais
                    // Switch para identificar os comandos
                    switch(command[0]){
                        case "addGroup":
                            rabbitmq.createExchange(command[1]); // Cria um exchange no RabbitMQ, representando um grupo
                            rabbitmq.addUser(command[1], userName); // Adiciona ao grupo após criá-lo
                            break;
                        case "addUser":
                            rabbitmq.addUser(command[1], command[2]); // Vincula uma fila ao exchange, significando adicionar uma pessoa ao grupo
                            break;
                        case "listUsers":
                            rabbitmq.listUsers(command[1]);
                            break;
                        case "delFromGroup":
                            rabbitmq.removeUser(command[1], command[2]); // Desvincula uma fila de um exchange, significando remover uma pessoa de um grupo
                            break;
                        case "removeGroup":
                            rabbitmq.deleteExchange(command[1]); // Exclui um exchange, significando a exclusão do grupo
                            break;
                        case "upload":
                            rabbitmq.sendFiles(command[1], target);
                            break;
                        case "createUser":
                            rabbitmq.createQueue(command[1]);
                            break;
                        default:
                            System.out.print("'" + command[0] + "' não é um comando.\n");
                    }
                    break;
                default:
                    if(message.equalsIgnoreCase("exit")) exit = true; // Verifica se o usuário deseja sair do chat
                    else if(target.equalsIgnoreCase("")) System.out.println("É necessário inserir um comando (Ex: '!nome_comando') ou um usuário (Ex: '@nome_usuário')"); // Verifica se o usuário não digitou um comando corretamente
                    else rabbitmq.sendMessage(message.getBytes("UTF-8"), target); // Envia a mensagem ao grupo ou usuário
            }
            
            if(exit) break; // Encerra o programa
            
            // Verificação para impressão no terminal
            if(!target.equalsIgnoreCase("")) System.out.print("\n" + target + ">> ");
            else System.out.print(">> ");
        }

        try{
            rabbitmq.endRabbitMQ(); // Encerra as conexões com o RabbitMQ
        }catch(Exception e){
            System.out.print(e);
            System.exit(0);
        }
        entry.close();
    }
}