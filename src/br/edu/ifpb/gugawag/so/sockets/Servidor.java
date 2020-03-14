package br.edu.ifpb.gugawag.so.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class Servidor {

    public static void main(String[] args) throws IOException {
        System.out.println("== Servidor ==");

        List<String> arquivos = new ArrayList<String>(Arrays.asList(
                "Guitarra.jpg",
                "Violao.jpg",
                "Teclado.png",
                "Bateria.drums.jpg"
        ));

        // Configurando o socket
        ServerSocket serverSocket = new ServerSocket(7001);
        Socket socket = serverSocket.accept();

        // pegando uma referência do canal de saída do socket. Ao escrever nesse canal, está se enviando dados para o
        // servidor
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // pegando uma referência do canal de entrada do socket. Ao ler deste canal, está se recebendo os dados
        // enviados pelo servidor
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        // laço infinito do servidor
        while (true) {
            System.out.println("Cliente: " + socket.getInetAddress());

            String mensagem = dis.readUTF();
            System.out.println(mensagem);

            String[] splitTemp = mensagem.split(" ");
            String command = splitTemp[0];
            String diretorio = splitTemp[1];
            String fileName1 = "";
            String fileName2 = "";

            if(splitTemp.length > 2) {
                fileName1 = splitTemp[2];
            }

            if(splitTemp.length > 3){
                fileName2 = splitTemp[3];
            }


            if(command.contains("readdir")){
                List<String> arquivosPasta;
                arquivosPasta = getFilesFromDiretorio(diretorio);
                dos.writeUTF("READDIR: " + arquivosPasta);

            }else if(command.contains("rename")){
                String result = renameFile(diretorio, fileName1, fileName2);
                dos.writeUTF("RENAME: " + result);

            }else if(command.contains("create")) {
                String result = createFile(diretorio, fileName1);
                dos.writeUTF("CREATE: " + result);
            }else if(command.contains("remove")) {
                removeFile(diretorio, fileName1);
                dos.writeUTF("REMOVE: OK");
            }
        }
        /*
         * Observe o while acima. Perceba que primeiro se lê a mensagem vinda do cliente (linha 29, depois se escreve
         * (linha 32) no canal de saída do socket. Isso ocorre da forma inversa do que ocorre no while do Cliente2,
         * pois, de outra forma, daria deadlock (se ambos quiserem ler da entrada ao mesmo tempo, por exemplo,
         * ninguém evoluiria, já que todos estariam aguardando.
         */
    }

    public static List<String> getFilesFromDiretorio(String diretorio) {
        List<String> arquivosPasta = new ArrayList<String>();

        try(Stream<Path> paths = Files.walk(Paths.get(diretorio))){
            paths
                    .filter(Files::isRegularFile)
                    .forEach(x -> arquivosPasta.add(x.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arquivosPasta;
    }

    public static String createFile(String diretorio, String fileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        Files.createFile(p);
        return "Operação OK";
    }

    public static String renameFile(String diretorio, String fileName, String newFileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        if(Files.exists(p)){
            Files.move(p, p.resolveSibling(newFileName));
            return "Successful";
        }else {
            return "Failed -> File doesn't exist!";
        }
    }

    public static String removeFile(String diretorio, String fileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        if(Files.exists(p)){
            Files.delete(p);
            return "Remove successful";
        } else {
            return "File doesn't exist!";
        }
    }
}
