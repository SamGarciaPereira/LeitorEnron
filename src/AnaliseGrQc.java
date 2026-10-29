/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// rede de coautoria GR-QC (Pajek): componentes, ciclo e coeficiente de agrupamento.
public class AnaliseGrQc {

    public static void main(String[] args) throws Exception {
        String arquivo = args.length > 0 ? args[0] : "CA-GrQc_teste.net";
        GrafoEnron g = GrafoEnron.pajekLoader(arquivo);

        System.out.println("Vértices: " + g.getNumVertices());
        System.out.println("Arestas (não direcionadas): " + g.getNumArestasNaoDirecionadas());

        // 2.1 a 2.4 - componentes
        List<List<Integer>> comps = g.componentes();
        System.out.println("\n2.1) Número de componentes: " + comps.size());
        List<Integer> maior = comps.get(0);
        System.out.println("2.2) Maior componente: C1, com " + maior.size() + " autores");
        System.out.println("2.3) Tamanho do maior componente: " + maior.size() + " vértices ("
                + String.format("%.2f", 100.0 * maior.size() / g.getNumVertices()) + "% do grafo)");
        System.out.println("2.4) Lista de componentes:");
        g.imprimeComponentes(20, 10);
        g.salvaComponentes("componentes_grqc.txt");
        System.out.println("Lista completa gravada em componentes_grqc.txt");

        // ciclo
        int ciclomatico = g.numeroCiclomatico();
        System.out.println("\nO grafo é cíclico? " + (g.ehCiclicoNaoDirecionado() ? "SIM" : "NÃO")
                + " (E - V + C = " + ciclomatico + " arestas além de uma floresta geradora)");

        // 4.1 - coeficientes
        double[] c = g.coeficientesAgrupamento();
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                Paths.get("agrupamento_grqc.txt"), StandardCharsets.UTF_8))) {
            out.println("autor\tgrau\tcoeficiente");
            for (int i = 0; i < c.length; i++) {
                out.println(g.getInformacao(i) + "\t" + g.grau(i) + "\t" + c[i]);
            }
        }
        System.out.println("\n4.1) Coeficiente local de cada vértice gravado em agrupamento_grqc.txt");
        System.out.printf("Coeficiente médio (todos os vértices): %.6f%n", g.coeficienteMedio(c, true));
        System.out.printf("Coeficiente médio (só grau >= 2):      %.6f%n", g.coeficienteMedio(c, false));

        // 4.2 - top 20
        System.out.println("\n4.2) Top 20 vértices com maior coeficiente local (empate: maior grau):");
        int pos = 1;
        for (int v : g.topAgrupamento(c, 20)) {
            System.out.printf("%2dº: autor %s (grau %d, C = %.4f)%n", pos++, g.getInformacao(v), g.grau(v), c[v]);
        }
    }
}
