/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * testes das questões 1, 2 e 3 em grafos pequenos com resposta conhecida.
 * execução: java TesteComponentes
 */
public class TesteComponentes {

    private static int falhas = 0;

    public static void main(String[] args) {
        // G1: DAG simples 0 -> 1 -> 2 e 0 -> 2
        GrafoEnron g1 = grafo(3, new int[][]{{0, 1}, {1, 2}, {0, 2}});
        confere("G1 acíclico", !g1.ehCiclico());
        confere("G1 1 componente", g1.componentes().size() == 1);
        confere("G1 conexo", g1.ehConexo());

        // G2: ciclo 0 -> 1 -> 2 -> 0 e aresta solta 3 -> 4
        GrafoEnron g2 = grafo(5, new int[][]{{0, 1}, {1, 2}, {2, 0}, {3, 4}});
        confere("G2 cíclico", g2.ehCiclico());
        confere("G2 ciclo 0->1->2->0", g2.encontraCiclo().equals(List.of(0, 1, 2, 0)));
        confere("G2 componentes {0,1,2} e {3,4}",
                g2.componentes().equals(List.of(List.of(0, 1, 2), List.of(3, 4))));
        confere("G2 desconexo", !g2.ehConexo());

        // G3: 0 -> 1 <- 2. não há caminho dirigido entre 0 e 2, mas ignorando o
        // sentido eles estão ligados por 1: fracamente conexo.
        GrafoEnron g3 = grafo(3, new int[][]{{0, 1}, {2, 1}});
        confere("G3 acíclico", !g3.ehCiclico());
        confere("G3 1 componente (sentido ignorado)", g3.componentes().size() == 1);
        confere("G3 conexo", g3.ehConexo());

        // G4: vértices isolados contam como componentes próprios
        GrafoEnron g4 = grafo(4, new int[][]{{0, 1}});
        confere("G4 3 componentes", g4.componentes().size() == 3);
        confere("G4 desconexo", !g4.ehConexo());

        // G5: laço 0 -> 0 é um ciclo de uma aresta
        GrafoEnron g5 = grafo(2, new int[][]{{0, 0}, {0, 1}});
        confere("G5 cíclico (laço)", g5.ehCiclico());

        // G6: losango 0->1, 0->2, 1->3, 2->3. a DFS reencontra 3 já concluído
        // (PRETO) pelo segundo ramo; isso NÃO é ciclo.
        GrafoEnron g6 = grafo(4, new int[][]{{0, 1}, {0, 2}, {1, 3}, {2, 3}});
        confere("G6 acíclico (losango)", !g6.ehCiclico());

        // G7: ciclo de duas arestas (A envia para B e B responde para A)
        GrafoEnron g7 = grafo(2, new int[][]{{0, 1}, {1, 0}});
        confere("G7 cíclico (ida e volta)", g7.ehCiclico());

        // G8: grafo vazio
        GrafoEnron g8 = new GrafoEnron(0);
        confere("G8 vazio acíclico", !g8.ehCiclico());
        confere("G8 vazio 0 componentes", g8.componentes().isEmpty());
        confere("G8 vazio conexo (convenção)", g8.ehConexo());

        // G9: cadeia longa de 300 mil vértices, sem estouro de pilha
        int n = 300_000;
        GrafoEnron g9 = new GrafoEnron(n);
        for (int i = 0; i < n - 1; i++) g9.cria_adjacencia(i, i + 1, 1);
        confere("G9 cadeia longa acíclica", !g9.ehCiclico());
        g9.cria_adjacencia(n - 1, 0, 1);
        confere("G9 cadeia fechada cíclica", g9.ehCiclico());
        confere("G9 ciclo com " + n + " arestas", g9.encontraCiclo().size() == n + 1);
        confere("G9 conexo", g9.ehConexo());

        // grafos aleatórios: compara com implementações independentes
        Random r = new Random(42);
        for (int t = 0; t < 2000; t++) {
            int v = 1 + r.nextInt(12);
            int e = r.nextInt(v * 2);
            GrafoEnron g = new GrafoEnron(v);
            List<int[]> arestas = new ArrayList<>();
            for (int k = 0; k < e; k++) {
                int a = r.nextInt(v), b = r.nextInt(v);
                g.cria_adjacencia(a, b, 1);
                arestas.add(new int[]{a, b});
            }
            if (g.ehCiclico() != cicloPorRemocao(v, arestas)) {
                confere("aleatório ciclo #" + t, false);
            }
            List<Integer> c = g.encontraCiclo();
            if (!c.isEmpty() && !cicloValido(c, arestas)) {
                confere("aleatório ciclo válido #" + t, false);
            }
            if (g.componentes().size() != componentesUnionFind(v, arestas)) {
                confere("aleatório componentes #" + t, false);
            }
        }
        confere("2000 grafos aleatórios", true);

        System.out.println(falhas == 0 ? "\nTODOS OS TESTES PASSARAM" : "\nFALHAS: " + falhas);
        if (falhas > 0) System.exit(1);
    }

    private static GrafoEnron grafo(int n, int[][] arestas) {
        GrafoEnron g = new GrafoEnron(n);
        for (int i = 0; i < n; i++) g.seta_informacao(i, "v" + i);
        for (int[] a : arestas) g.cria_adjacencia(a[0], a[1], 1);
        return g;
    }

    private static void confere(String nome, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALHA] ") + nome);
        if (!ok) falhas++;
    }

    // referência para ciclo: algoritmo de Kahn (remove vértices de grau de entrada 0).
    // sobra vértice sem remover <=> há ciclo.
    private static boolean cicloPorRemocao(int n, List<int[]> arestas) {
        boolean[][] m = new boolean[n][n];
        for (int[] a : arestas) m[a[0]][a[1]] = true;
        int[] grau = new int[n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (m[i][j]) grau[j]++;
        boolean[] removido = new boolean[n];
        int removidos = 0;
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (int i = 0; i < n; i++) {
                if (!removido[i] && grau[i] == 0) {
                    removido[i] = true;
                    removidos++;
                    mudou = true;
                    for (int j = 0; j < n; j++) if (m[i][j]) grau[j]--;
                }
            }
        }
        return removidos < n;
    }

    private static boolean cicloValido(List<Integer> c, List<int[]> arestas) {
        if (!c.get(0).equals(c.get(c.size() - 1))) return false;
        for (int i = 0; i + 1 < c.size(); i++) {
            boolean existe = false;
            for (int[] a : arestas) if (a[0] == c.get(i) && a[1] == c.get(i + 1)) existe = true;
            if (!existe) return false;
        }
        return true;
    }

    // referência para componentes: union-find sobre as arestas sem sentido.
    private static int componentesUnionFind(int n, List<int[]> arestas) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        int comps = n;
        for (int[] a : arestas) {
            int x = raiz(p, a[0]), y = raiz(p, a[1]);
            if (x != y) {
                p[x] = y;
                comps--;
            }
        }
        return comps;
    }

    private static int raiz(int[] p, int x) {
        while (p[x] != x) x = p[x] = p[p[x]];
        return x;
    }
}
