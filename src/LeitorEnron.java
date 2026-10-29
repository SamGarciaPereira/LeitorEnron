/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * lê a base de e-mails e monta o grafo: vértice = endereço, aresta A -> B com
 * peso igual ao número de mensagens de A para B. apenas o campo To é
 * considerado destinatário.
 */
public class LeitorEnron {

    // espaço, vírgula, < > e ; são os separadores de endereços no cabeçalho.
    // ex.: em "Ana <ana@enron.com>, beto@enron.com" casa ana@enron.com e beto@enron.com.
    private static final Pattern EMAIL = Pattern.compile("[^\\s,<>;]+@[^\\s,<>;]+");

    private final Map<String, Integer> indice = new HashMap<>();   // e-mail -> índice do vértice
    private final List<String> emails = new ArrayList<>();         // índice -> e-mail
    private final Map<Long, Integer> pesos = new HashMap<>();      // par (origem, destino) -> peso
    private final Set<String> idsVistos = new HashSet<>();         // message-id já processados

    public static GrafoEnron carregar(String raiz) throws IOException {
        return new LeitorEnron().construir(Paths.get(raiz));
    }

    // fluxo: 1) lê todos os arquivos  2) cria um vértice por e-mail  3) cria as arestas com os pesos.
    private GrafoEnron construir(Path raiz) throws IOException {
        // etapa 1: percorre todas as subpastas e processa cada arquivo (um arquivo = um e-mail).
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            arquivos.filter(Files::isRegularFile).forEach(this::processa);
        }

        // o grafo só pode ser criado aqui: o número de vértices é fixado no
        // construtor e só é conhecido após a leitura de todos os arquivos.
        GrafoEnron g = new GrafoEnron(emails.size());
        // etapa 2: o vértice i recebe como rótulo o e-mail de índice i.
        for (int i = 0; i < emails.size(); i++) {
            g.seta_informacao(i, emails.get(i));
        }
        // etapa 3: cada par (origem, destino) contado vira uma aresta com seu peso.
        for (Map.Entry<Long, Integer> e : pesos.entrySet()) {
            long chave = e.getKey();
            // >>> 32 recupera a origem; o cast para int mantém apenas o destino.
            g.cria_adjacencia((int) (chave >>> 32), (int) chave, e.getValue());
        }
        return g;
    }

    // fases: lê o cabeçalho -> descarta e-mail repetido -> soma 1 em cada par remetente x destinatário.
    private void processa(Path arquivo) {
        // o mapa define os únicos cabeçalhos aproveitados. qualquer outro devolve
        // null em campos.get e é descartado sem verificação adicional.
        Map<String, StringBuilder> campos = new HashMap<>();
        campos.put("message-id", new StringBuilder());
        campos.put("from", new StringBuilder());
        campos.put("to", new StringBuilder());

        // o cabeçalho termina na primeira linha vazia. linha iniciada por espaço ou
        // tabulação é continuação do campo anterior, caso frequente no campo To.
        // iso-8859-1 aceita qualquer byte: a base contém sequências inválidas em utf-8.
        try (BufferedReader r = Files.newBufferedReader(arquivo, StandardCharsets.ISO_8859_1)) {
            StringBuilder atual = null;
            String linha;
            while ((linha = r.readLine()) != null && !linha.isEmpty()) {
                char c = linha.charAt(0);
                if (c == ' ' || c == '\t') {
                    if (atual != null) atual.append(' ').append(linha);
                } else {
                    int p = linha.indexOf(':');
                    atual = p < 0 ? null : campos.get(linha.substring(0, p).toLowerCase());
                    if (atual != null) atual.append(linha.substring(p + 1));
                }
            }
        } catch (IOException e) {
            return;   // arquivo ilegível é descartado
        }

        // a mesma mensagem consta na caixa do remetente e na do destinatário.
        // o message-id garante que ela seja contabilizada uma única vez.
        String id = campos.get("message-id").toString().trim();
        if (!id.isEmpty() && !idsVistos.add(id)) return;

        for (String origem : enderecos(campos.get("from"))) {
            for (String destino : enderecos(campos.get("to"))) {
                if (origem.equals(destino)) continue;   // mensagem para si mesmo não gera aresta
                // origem nos 32 bits altos e destino nos baixos: um único long
                // identifica o par e dispensa um mapa de mapas.
                long chave = ((long) indiceDe(origem) << 32) | indiceDe(destino);
                pesos.merge(chave, 1, Integer::sum);   // par novo começa em 1; existente soma 1
            }
        }
    }

    //endereços distintos do texto. o set elimina destinatário repetido na mesma mensagem.
    private static Set<String> enderecos(CharSequence texto) {
        Set<String> resultado = new HashSet<>();
        Matcher m = EMAIL.matcher(texto);
        while (m.find()) {
            resultado.add(m.group().toLowerCase().replace("\"", ""));
        }
        return resultado;
    }

    //índice do endereço, atribuído na primeira ocorrência.
    private int indiceDe(String email) {
        Integer i = indice.get(email);
        if (i == null) {
            i = emails.size();
            indice.put(email, i);
            emails.add(email);
        }
        return i;
    }

    //execução isolada do leitor, sem menu, para conferir as contagens.
    public static void main(String[] args) throws IOException {
        String raiz = args.length > 0 ? args[0] : "maildir";
        LeitorEnron leitor = new LeitorEnron();
        leitor.construir(Paths.get(raiz));
        System.out.println("Vértices: " + leitor.emails.size());
        System.out.println("Arestas: " + leitor.pesos.size());
    }
}
