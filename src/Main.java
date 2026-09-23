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
            System.out.println("2 - Caminho crítico entre dois e-mails");
            System.out.println("3 - Exibir métricas gerais do grafo");
            System.out.println("4 - Busca em Profundidade DFS");
            System.out.println("5 - Busca em Largura BFS");
            System.out.println("6 - Nós a distância D de um e-mail");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            switch (in.nextLine().trim()) {
                case "1": {
                    int v = lerVertice(g, in, "E-mail: ");
                    if (v >= 0) mostraDestinos(g, v);
                    break;
                }
                case "2": {
                    int a = lerVertice(g, in, "E-mail de origem (A): ");
                    if (a < 0) break;
                    int c = lerVertice(g, in, "E-mail de destino (C): ");
                    if (c < 0) break;
                    g.imprime_caminho_critico(a, c);
                    break;
                }
                case "3": {
                    System.out.println("\nMÉTRICAS DO GRAFO:");
                    System.out.println("Número de vértices: " + g.getNumVertices());
                    System.out.println("Número de arestas: " + g.getNumArestas());
                    g.imprimeTop20Graus();
                    break;
                }
                case "4": {
                    int x = lerVertice(g, in, "E-mail de origem (X): ");
                    if (x < 0) break;
                    int y = lerVertice(g, in, "E-mail de destino (Y): ");
                    if (y < 0) break;
                    g.imprimeDFS(x, y);
                    break;
                }
                case "5": {
                    int x = lerVertice(g, in, "E-mail de origem (X): ");
                    if (x < 0) break;
                    int y = lerVertice(g, in, "E-mail de destino (Y): ");
                    if (y < 0) break;
                    g.imprimeBFS(x, y);
                    break;
                }
                case "6": {
                    int n = lerVertice(g, in, "E-mail (N): ");
                    if (n < 0) break;
                    System.out.print("Distância (D): ");
                    try {
                        int d = Integer.parseInt(in.nextLine().trim());
                        g.imprimeNosADistancia(n, d);
                    } catch (NumberFormatException e) {
                        System.out.println("Distância inválida.");
                    }
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
