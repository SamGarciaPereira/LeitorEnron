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
        String raiz = args.length > 0 ? args[0] : "maildir";   // pasta da base; padrão "maildir"

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
            System.out.println("7 - Verificar se o grafo é cíclico");
            System.out.println("8 - Componentes do grafo");
            System.out.println("9 - Verificar se o grafo é conexo");
            System.out.println("10 - Árvore Geradora Mínima (Kruskal)");
            System.out.println("11 - Encontrar componentes (lista todos os membros)");
            System.out.println("12 - Verificar se um conjunto de e-mails é clique");
            System.out.println("13 - Verificar se um conjunto de e-mails é clique maximal");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            switch (in.nextLine().trim()) {
                case "1": {   // lista as arestas de saída do vértice
                    int v = lerVertice(g, in, "E-mail: ");
                    if (v >= 0) mostraDestinos(g, v);
                    break;
                }
                case "2": {   // Dijkstra adaptado (custo 1/peso): caminho de maior dependência
                    int a = lerVertice(g, in, "E-mail de origem (A): ");
                    if (a < 0) break;
                    int c = lerVertice(g, in, "E-mail de destino (C): ");
                    if (c < 0) break;
                    g.imprime_caminho_critico(a, c);
                    break;
                }
                case "3": {   // contagens + ranking de graus de saída e entrada
                    System.out.println("\nMÉTRICAS DO GRAFO:");
                    System.out.println("Número de vértices: " + g.getNumVertices());
                    System.out.println("Número de arestas: " + g.getNumArestas());
                    g.imprimeTop20Graus();
                    break;
                }
                case "4": {   // DFS: encontra algum caminho de X até Y
                    int x = lerVertice(g, in, "E-mail de origem (X): ");
                    if (x < 0) break;
                    int y = lerVertice(g, in, "E-mail de destino (Y): ");
                    if (y < 0) break;
                    g.imprimeDFS(x, y);
                    break;
                }
                case "5": {   // BFS: caminho com o menor número de saltos
                    int x = lerVertice(g, in, "E-mail de origem (X): ");
                    if (x < 0) break;
                    int y = lerVertice(g, in, "E-mail de destino (Y): ");
                    if (y < 0) break;
                    g.imprimeBFS(x, y);
                    break;
                }
                case "6": {   // BFS em camadas: vértices exatamente a D saltos de N
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
                case "7":
                    g.imprimeCiclo();
                    break;
                case "8": {
                    g.imprimeComponentes(20, 10);
                    String arq = "componentes.txt";
                    g.salvaComponentes(arq);
                    System.out.println("\nLista completa de componentes gravada em " + arq);
                    break;
                }
                case "9":
                    g.imprimeConexidade();
                    break;
                case "10":   // Kruskal + union-find: floresta de menor peso total
                    g.imprimeKruskal();
                    break;
                case "11":
                    g.encontraComponentes();
                    break;
                case "12": {
                    List<Integer> v = lerConjunto(g, in);
                    if (v != null) System.out.println(g.eClique(v) ? "É clique." : "NÃO é clique.");
                    break;
                }
                case "13": {
                    List<Integer> v = lerConjunto(g, in);
                    if (v != null) System.out.println(g.eMaximal(v) ? "É clique maximal." : "NÃO é clique maximal.");
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

    /** lê e-mails separados por vírgula ou espaço; null se algum não estiver na base. */
    private static List<Integer> lerConjunto(GrafoEnron g, Scanner in) {
        System.out.print("E-mails (separados por vírgula ou espaço): ");
        List<Integer> v = new java.util.ArrayList<>();
        for (String e : in.nextLine().trim().toLowerCase().split("[,\\s]+")) {
            if (e.isEmpty()) continue;
            int i = g.indiceDe(e);
            if (i < 0) {
                System.out.println("E-mail não encontrado na base: " + e);
                return null;
            }
            v.add(i);
        }
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
