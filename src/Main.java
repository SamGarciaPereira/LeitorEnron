/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.util.List;
import java.util.Scanner;

/**
 * ponto de entrada. o grafo é carregado uma única vez, antes do menu, porque a
 * leitura da base percorre todos os arquivos.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String raiz = args.length > 0 ? args[0] : "maildir";

        long inicio = System.currentTimeMillis();
        GrafoEnron g = LeitorEnron.carregar(raiz);
        System.out.println("Grafo carregado: " + g.getNumVertices() + " vértices ("
                + (System.currentTimeMillis() - inicio) / 1000 + " s)");

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n1 - Consultar destinos de um e-mail");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            switch (in.nextLine().trim()) {
                case "1": {
                    int v = lerVertice(g, in, "E-mail: ");
                    if (v >= 0) mostraDestinos(g, v);
                    break;
                }
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    /** lê um e-mail e devolve o índice do vértice, ou -1 se não estiver na base. */
    private static int lerVertice(GrafoEnron g, Scanner in, String pergunta) {
        System.out.print(pergunta);
        int v = g.indiceDe(in.nextLine().trim().toLowerCase());
        if (v < 0) System.out.println("E-mail não encontrado na base.");
        return v;
    }

    /** exibe até 20 destinos do vértice v e o peso de cada aresta. */
    private static void mostraDestinos(GrafoEnron g, int v) {
        List<GrafoEnron.Aresta> arestas = g.arestas(v);
        System.out.println(g.getInformacao(v) + " enviou para " + arestas.size() + " destinatário(s):");
        for (int i = 0; i < Math.min(20, arestas.size()); i++) {
            GrafoEnron.Aresta a = arestas.get(i);
            System.out.println("  -> " + g.getInformacao(a.getDestino()) + " (peso " + a.getPeso() + ")");
        }
        if (arestas.size() > 20) {
            System.out.println("  ... e mais " + (arestas.size() - 20));
        }
    }
}
