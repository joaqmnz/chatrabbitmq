# ChatRabbitMQ

### Projeto de Desenvolvimento de um Chat via linha de comando utilizando RabbitMQ

- ## Descrição

1. Como compilar e executar

No diretório raíz do programa, execute o seguinte comando:

```
mvn compile assembly:single
```

Será gerado um arquivo `ChatRabbitMQ-1.0-SNAPSHOT-jar-with-dependencies.jar` no diretório `target` do projeto

Para execução, basta inserir o seguinte comando:

```
java -Xms512m -Xmx1024m -jar target/ChatRabbitMQ-1.0-SNAPSHOT-jar-with-dependencies.jar
```
