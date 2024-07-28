package br.ufs.dcomp.ChatRabbitMQ;

public class Functools
{
    public Functools(){};
    
    public void sleep(Integer time)
    {
        // Faz a Thread atual esperar "time"ms
        try{
            Thread.sleep(time);
        } catch(Exception error){
            System.out.println(error);
            System.exit(0);
        }
    }
    
    public void clear() throws Exception
    {
        new ProcessBuilder("clear").inheritIO().start().waitFor(); // Limpa o terminal
    }
}