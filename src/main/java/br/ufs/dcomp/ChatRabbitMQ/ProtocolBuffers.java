package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Date;
import java.text.SimpleDateFormat;
import com.google.protobuf.ByteString;

public class ProtocolBuffers
{
    public ProtocolBuffers(String emissor) {}
    
    public PadraoMensagem.Mensagem createMessage(byte[] message, String emissor, String group, String mimeType, String filename)
    {
        Date now = new Date(); // Obtém a data e horário atuais
        String date = new SimpleDateFormat("dd/MM/yyyy").format(now); // Formata a data atual
        String time = new SimpleDateFormat("HH:mm:ss").format(now); // Formata o horário atual
        
        PadraoMensagem.Conteudo.Builder buildConteudo = PadraoMensagem.Conteudo.newBuilder();
        buildConteudo.setTipo(mimeType);
        try{
            buildConteudo.setCorpo(ByteString.copyFrom(message));
        } catch(Exception e){
            System.out.println(e);
        }
        buildConteudo.setNome(filename);
        
        PadraoMensagem.Mensagem.Builder buildMessage = PadraoMensagem.Mensagem.newBuilder();
        buildMessage.setEmissor(emissor);
        buildMessage.setData(date);
        buildMessage.setHora(time);
        buildMessage.setGrupo(group);
        buildMessage.setConteudo(buildConteudo);
        
        return buildMessage.build();
    }
}