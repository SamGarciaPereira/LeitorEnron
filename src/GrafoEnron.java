/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// grafo direcionado, ponderado e rotulado. não depende do domínio de e-mail.
// representação por lista de adjacência: cada vértice guarda só as arestas que saem dele.
public class GrafoEnron {

    // seta i -> destino com peso (no projeto: quantos e-mails i mandou para destino).
    public static class Aresta {
        private final int destino;   // índice do vértice de destino
        private int peso;   // nº de mensagens 

        Aresta(int destino, int peso) {
            this.destino = destino;
            this.peso = peso;
        }

        public int getDestino() {
            return destino;
        }

        public int getPeso() {
            return peso;
        }
    }

    // rótulo (o e-mail) + lista de arestas de saída.
    private static class Vertice {
        String informacao = "";   // o rótulo (e-mail)
        List<Aresta> adjacencias = new ArrayList<>();
    }

    private static final int INFINITO = Integer.MAX_VALUE;

    private final Vertice[] vertices;
    private final int numVertices;

    // rótulo (e-mail) -> índice. evita varrer todos os vértices a cada consulta.
    private final Map<String, Integer> indicePorInformacao = new HashMap<>();   

    public GrafoEnron(int numVertices) {
        this.numVertices = numVertices;
        this.vertices = new Vertice[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vertice();
        }
    }

    public void cria_adjacencia(int i, int j, int P) {
        if (!verticeValido(i) || !verticeValido(j)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // aresta já existente tem o peso substituído, não acumulado.
        for (Aresta a : vertices[i].adjacencias) {
            if (a.destino == j) {
                a.peso = P;
                return;
            }
        }
        vertices[i].adjacencias.add(new Aresta(j, P));
    }

    public void remove_adjacencia(int i, int j) {
        if (!verticeValido(i) || !verticeValido(j)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // a remoção exige iterator: retirar elementos de uma lista percorrida
        // por for-each lança ConcurrentModificationException.
        Iterator<Aresta> it = vertices[i].adjacencias.iterator();
        while (it.hasNext()) {
            if (it.next().destino == j) {
                it.remove();
                return;
            }
        }
    }

    public void imprime_adjacencias() {
        for (int i = 0; i < numVertices; i++) {
            System.out.print(i + " (" + vertices[i].informacao + "):");
            for (Aresta a : vertices[i].adjacencias) {
                System.out.print(" -> " + a.destino + "[" + a.peso + "]");
            }
            System.out.println();
        }
    }

    public void seta_informacao(int i, String V) {
        if (!verticeValido(i)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // o rótulo anterior deixa de apontar para este vértice.
        indicePorInformacao.remove(vertices[i].informacao, i);
        vertices[i].informacao = V;
        indicePorInformacao.put(V, i);
    }

    public String getInformacao(int i) {
        return verticeValido(i) ? vertices[i].informacao : null;
    }

    // copia os índices dos vizinhos de i em adj e devolve quantos são.
    public int adjacentes(int i, int[] adj) {
        if (!verticeValido(i)) {
            return 0;
        }
        int qtd = vertices[i].adjacencias.size();
        for (int k = 0; k < qtd; k++) {
            adj[k] = vertices[i].adjacencias.get(k).destino;
        }
        return qtd;
    }

    public int getNumVertices() {
        return numVertices;
    }

    // índice do vértice com esse rótulo, ou -1 se não existir.
    public int indiceDe(String informacao) {
        return indicePorInformacao.getOrDefault(informacao, -1);
    }

    //arestas de saída do vértice i. a lista é somente leitura.
    public List<Aresta> arestas(int i) {
        if (!verticeValido(i)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(vertices[i].adjacencias);
    }

    private boolean verticeValido(int i) {
        return i >= 0 && i < numVertices;
    }

    //distância de custo mínimo da origem até cada vértice.
    // a cada rodada fixa o vértice mais próximo ainda aberto e tenta melhorar seus vizinhos.
    // a escolha do menor é uma varredura linear (sem fila de prioridade): O(V²).
    public int[] dijkstra(int origem) {
        int[] dist = new int[numVertices];
        boolean[] visitado = new boolean[numVertices];
        java.util.Arrays.fill(dist, INFINITO);

        if (!verticeValido(origem)) {
            return dist;
        }
        dist[origem] = 0;

        for (int cont = 0; cont < numVertices; cont++) {
            // seleciona o vértice não visitado de menor distância acumulada.
            int u = -1;
            int menor = INFINITO;
            for (int i = 0; i < numVertices; i++) {
                if (!visitado[i] && dist[i] < menor) {
                    menor = dist[i];
                    u = i;
                }
            }
            if (u == -1) break;   // o restante do grafo é inalcançável

            // marcar como visitado impede reprocessamento e trata os ciclos.
            visitado[u] = true;

            // relaxamento: se passar por u deixa o vizinho mais barato, atualiza.
            for (Aresta a : vertices[u].adjacencias) {
                if (!visitado[a.destino] && dist[u] + a.peso < dist[a.destino]) {
                    dist[a.destino] = dist[u] + a.peso;
                }
            }
        }

        return dist;
    }

    public void imprime_dijkstra(int origem) {
        int[] dist = dijkstra(origem);
        System.out.println("Distâncias mínimas a partir do vértice " + origem
                + " (" + getInformacao(origem) + "):");
        for (int i = 0; i < numVertices; i++) {
            String rotulo = getInformacao(i);
            if (dist[i] == INFINITO) {
                System.out.println("  -> " + i + " (" + rotulo + "): inalcançável");
            } else {
                System.out.println("  -> " + i + " (" + rotulo + "): " + dist[i]);
            }
        }
    }

    // caminho de maior dependência acumulada entre origem e destino, de forma aproximada:
    // adapta o Dijkstra usando 1/peso como custo da aresta, já que peso maior representa
    // maior dependência. minimizar a soma de 1/peso tende a preferir arestas de peso alto,
    // aproximando (sem garantir) o caminho de maior soma de pesos originais.
    public CaminhoCritico caminhoCritico(int origem, int destino) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return new CaminhoCritico(Collections.emptyList(), 0);
        }

        double[] custo = new double[numVertices];
        int[] anterior = new int[numVertices];   // de onde se chegou a cada vértice; reconstrói o caminho
        boolean[] visitado = new boolean[numVertices];
        java.util.Arrays.fill(custo, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(anterior, -1);
        custo[origem] = 0;

        for (int cont = 0; cont < numVertices; cont++) {
            int u = -1;
            double menor = Double.POSITIVE_INFINITY;
            for (int i = 0; i < numVertices; i++) {
                if (!visitado[i] && custo[i] < menor) {
                    menor = custo[i];
                    u = i;
                }
            }
            if (u == -1) break;

            // finalizar u e nunca reabri-lo garante que o caminho reconstruído por
            // "anterior" não forme ciclo, mesmo que o grafo original tenha um.
            visitado[u] = true;
            if (u == destino) break;

            for (Aresta a : vertices[u].adjacencias) {
                if (visitado[a.destino]) continue;
                double custoAresta = 1.0 / a.peso;
                if (custo[u] + custoAresta < custo[a.destino]) {
                    custo[a.destino] = custo[u] + custoAresta;
                    anterior[a.destino] = u;
                }
            }
        }

        if (custo[destino] == Double.POSITIVE_INFINITY) {
            return new CaminhoCritico(Collections.emptyList(), 0);
        }

        // volta do destino até a origem (anterior == -1), somando os pesos originais.
        List<Integer> caminho = new ArrayList<>();
        int dependencia = 0;
        for (int v = destino; v != -1; v = anterior[v]) {
            caminho.add(v);
            if (anterior[v] != -1) {
                dependencia += pesoAresta(anterior[v], v);
            }
        }
        Collections.reverse(caminho);
        return new CaminhoCritico(caminho, dependencia);
    }

    private int pesoAresta(int i, int j) {
        for (Aresta a : vertices[i].adjacencias) {
            if (a.destino == j) return a.peso;
        }
        return 0;
    }

    public void imprime_caminho_critico(int origem, int destino) {
        CaminhoCritico r = caminhoCritico(origem, destino);
        if (r.caminho.isEmpty()) {
            System.out.println("Não há caminho entre " + getInformacao(origem)
                    + " e " + getInformacao(destino) + ".");
            return;
        }
        System.out.print("Caminho crítico de " + getInformacao(origem) + " até "
                + getInformacao(destino) + ": ");
        for (int i = 0; i < r.caminho.size(); i++) {
            System.out.print(getInformacao(r.caminho.get(i)));
            if (i < r.caminho.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
        System.out.println("Dependência acumulada: " + r.dependenciaAcumulada);
    }

    public int getNumArestas() {
        int totalArestas = 0;
        for (int i = 0; i < numVertices; i++) {
            totalArestas += vertices[i].adjacencias.size();
        }
        return totalArestas;
    }

    // calcula e exibe os 20 indivíduos com maior grau de saída e entrada
    // grau de saída = nº de destinatários distintos; grau de entrada = nº de remetentes distintos.
    public void imprimeTop20Graus() {
        int[] grauSaida = new int[numVertices];
        int[] grauEntrada = new int[numVertices];

        // calcula os graus de saída e entrada
        for (int i = 0; i < numVertices; i++) {
            int qtdAdjacencias = vertices[i].adjacencias.size();
            grauSaida[i] = qtdAdjacencias;

            for (int j = 0; j < qtdAdjacencias; j++) {
                Aresta a = vertices[i].adjacencias.get(j);
                grauEntrada[a.destino]++;
            }
        }

        System.out.println("\n--- Top 20 Indivíduos com Maior Grau de Saída ---");
        imprimeTop20(grauSaida);

        System.out.println("\n--- Top 20 Indivíduos com Maior Grau de Entrada ---");
        imprimeTop20(grauEntrada);
    }

    // ordena e imprime os 20 maiores valores de um vetor de graus
    private void imprimeTop20(int[] graus) {
        int[] maioresGraus = new int[20];
        int[] indicesMaiores = new int[20];

        // -1 marca posição do ranking ainda vazia.
        for (int i = 0; i < 20; i++) {
            indicesMaiores[i] = -1;
            maioresGraus[i] = -1;
        }

        for (int i = 0; i < numVertices; i++) {
            int grauAtual = graus[i];

            // o ranking fica sempre ordenado (maior primeiro); só entra quem supera o 20º.
            // a inserção funciona como um passo do insertion sort.
            if (grauAtual > maioresGraus[19]) {
                int posicaoInsercao = 19;

                // empurra os menores para baixo para abrir espaço na posição correta
                while (posicaoInsercao > 0 && grauAtual > maioresGraus[posicaoInsercao - 1]) {
                    maioresGraus[posicaoInsercao] = maioresGraus[posicaoInsercao - 1];
                    indicesMaiores[posicaoInsercao] = indicesMaiores[posicaoInsercao - 1];
                    posicaoInsercao--;
                }

                // insere o novo grau e índice na posição aberta
                maioresGraus[posicaoInsercao] = grauAtual;
                indicesMaiores[posicaoInsercao] = i;
            }
        }

        for (int i = 0; i < 20; i++) {
            int v = indicesMaiores[i];
            if (v == -1 || maioresGraus[i] == 0) break;

            System.out.println((i + 1) + "º: " + getInformacao(v) + " (Grau: " + maioresGraus[i] + ")");
        }
    }

    // inicia a busca em profundidade retornando a lista de nós do caminho
    // a DFS encontra algum caminho, não necessariamente o mais curto (para isso, ver BFS).
    public List<Integer> buscaEmProfundidade(int origem, int destino) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return Collections.emptyList();
        }

        boolean[] visitado = new boolean[numVertices];
        List<Integer> caminho = new ArrayList<>();

        if (dfsRecursiva(origem, destino, visitado, caminho)) {
            return caminho;
        }

        return Collections.emptyList();
    }

    // recursão da busca, o vetor de visitados é compartilhado para evitar ciclos
    private boolean dfsRecursiva(int u, int destino, boolean[] visitado, List<Integer> caminho) {
        // 1) marca u e o coloca no caminho atual.
        visitado[u] = true;
        caminho.add(u);

        // 2) caso base: chegou ao destino.
        if (u == destino) {
            return true;
        }

        int qtdAdjacencias = vertices[u].adjacencias.size();

        // 3) desce recursivamente pelo primeiro vizinho não visitado; se ele achar, propaga o true.
        for (int i = 0; i < qtdAdjacencias; i++) {
            Aresta a = vertices[u].adjacencias.get(i);

            if (!visitado[a.destino]) {
                if (dfsRecursiva(a.destino, destino, visitado, caminho)) {
                    return true;
                }
            }
        }

        // 4) backtracking: nenhum vizinho levou ao destino, então u sai do caminho.
        caminho.remove(caminho.size() - 1);
        return false;
    }

    public void imprimeDFS(int origem, int destino) {
        List<Integer> caminho = buscaEmProfundidade(origem, destino);

        if (caminho.isEmpty()) {
            System.out.println("Não há caminho alcançável de " + getInformacao(origem)
                    + " até " + getInformacao(destino) + " (DFS).");
            return;
        }

        int saltos = caminho.size() - 1;
        System.out.println("\nCaminho DFS de " + getInformacao(origem) + " até " + getInformacao(destino) + ":");
        System.out.println("Total de indivíduos no caminho: " + caminho.size());
        System.out.println("Total de saltos: " + saltos);
        System.out.println("Trajeto:");

        for (int i = 0; i < caminho.size(); i++) {
            if (i == 0) {
                System.out.println("  [Origem]  " + getInformacao(caminho.get(i)));
            } else if (i == caminho.size() - 1) {
                System.out.println("  [Destino] " + getInformacao(caminho.get(i)));
            } else {
                System.out.println("    (" + i + ") -> " + getInformacao(caminho.get(i)));
            }
        }
    }

    // busca em largura de origem até destino. devolve o caminho (lista de índices)
    // ou lista vazia se destino não for alcançável. como a BFS explora o grafo em
    // camadas (distância 1, depois 2, ...), o caminho encontrado é o de menor
    // número de saltos. nosVisitados, se não for null, recebe a ordem de visita.
    public List<Integer> buscaEmLargura(int origem, int destino, List<Integer> nosVisitados) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return Collections.emptyList();
        }
 
        boolean[] visitado = new boolean[numVertices];
        int[] anterior = new int[numVertices];   // de qual vértice cada um foi descoberto
        Arrays.fill(anterior, -1);
 
        // fila FIFO: quem foi descoberto primeiro é expandido primeiro, garantindo as camadas.
        ArrayDeque<Integer> fila = new ArrayDeque<>();
        visitado[origem] = true;   // marca ao entrar na fila: nenhum vértice entra duas vezes
        fila.add(origem);
 
        boolean achou = false;
        while (!fila.isEmpty()) {
            int u = fila.poll();
            if (nosVisitados != null) nosVisitados.add(u);
 
            if (u == destino) {
                achou = true;
                break;
            }
 
            for (Aresta a : vertices[u].adjacencias) {
                // o vetor de visitados impede reentrar em vértices já vistos,
                // então ciclos do grafo não geram loop infinito.
                if (!visitado[a.destino]) {
                    visitado[a.destino] = true;
                    anterior[a.destino] = u;
                    fila.add(a.destino);
                }
            }
        }
 
        if (!achou) {
            return Collections.emptyList();
        }
 
        // reconstrói o caminho voltando do destino até a origem pelo vetor anterior
        // (a origem é o único vértice com anterior == -1). depois inverte para ficar origem -> destino.
        List<Integer> caminho = new ArrayList<>();
        for (int v = destino; v != -1; v = anterior[v]) {
            caminho.add(v);
        }
        Collections.reverse(caminho);
        return caminho;
    }
 
    public List<Integer> buscaEmLargura(int origem, int destino) {
        return buscaEmLargura(origem, destino, null);
    }
 
    public void imprimeBFS(int origem, int destino) {
        List<Integer> visitados = new ArrayList<>();
        List<Integer> caminho = buscaEmLargura(origem, destino, visitados);
 
        if (caminho.isEmpty()) {
            System.out.println("Não há caminho alcançável de " + getInformacao(origem)
                    + " até " + getInformacao(destino) + " (BFS).");
            System.out.println("Vértices visitados na busca: " + visitados.size());
            return;
        }
 
        System.out.println("\nCaminho BFS de " + getInformacao(origem) + " até " + getInformacao(destino) + ":");
        System.out.println("Vértices visitados na busca: " + visitados.size());
        System.out.println("Total de indivíduos no caminho: " + caminho.size());
        System.out.println("Total de saltos (menor possível): " + (caminho.size() - 1));
        System.out.println("Trajeto:");
 
        for (int i = 0; i < caminho.size(); i++) {
            if (i == 0) {
                System.out.println("  [Origem]  " + getInformacao(caminho.get(i)));
            } else if (i == caminho.size() - 1) {
                System.out.println("  [Destino] " + getInformacao(caminho.get(i)));
            } else {
                System.out.println("    (" + i + ") -> " + getInformacao(caminho.get(i)));
            }
        }
    }
 
    // nós que estão exatamente a D arestas de N. a distância usada é a menor
    // possível (em número de arestas), calculada por uma BFS em camadas a partir de N.
    // um nó alcançável em 1 e em 3 saltos está a distância 1, não 3.
    public List<Integer> nosADistancia(int n, int d) {
        List<Integer> resultado = new ArrayList<>();
        if (!verticeValido(n) || d < 0) {
            return resultado;
        }
 
        // distancia[] faz o papel do vetor de visitados e ainda guarda o nº de saltos até N.
        int[] distancia = new int[numVertices];
        Arrays.fill(distancia, -1);   // -1 = ainda não visitado; evita ciclos
        distancia[n] = 0;
 
        ArrayDeque<Integer> fila = new ArrayDeque<>();
        fila.add(n);
 
        while (!fila.isEmpty()) {
            int u = fila.poll();
 
            if (distancia[u] == d) {
                resultado.add(u);
                continue;   // não precisa expandir além da distância pedida
            }
 
            for (Aresta a : vertices[u].adjacencias) {
                if (distancia[a.destino] == -1) {
                    distancia[a.destino] = distancia[u] + 1;
                    fila.add(a.destino);
                }
            }
        }
 
        return resultado;
    }
 
    public void imprimeNosADistancia(int n, int d) {
        if (d < 0) {
            System.out.println("A distância deve ser maior ou igual a zero.");
            return;
        }
        List<Integer> nos = nosADistancia(n, d);
        System.out.println("\nNós a distância " + d + " de " + getInformacao(n) + ": " + nos.size());
        for (int v : nos) {
            System.out.println("  - " + getInformacao(v));
        }
    }

    // =====================================================================
    // QUESTÃO 1 - verificação de ciclo
    // =====================================================================

    // estados da DFS: BRANCO = não visitado, CINZA = na pilha de recursão
    // (caminho atual), PRETO = vértice e todos os seus descendentes concluídos.
    private static final int BRANCO = 0;
    private static final int CINZA = 1;
    private static final int PRETO = 2;

    // o grafo é cíclico se contém ao menos um ciclo dirigido.
    public boolean ehCiclico() {
        return !encontraCiclo().isEmpty();
    }

    // devolve um ciclo dirigido (v0 -> v1 -> ... -> v0) ou lista vazia se o grafo
    // for acíclico. a DFS percorre todos os vértices: num grafo dirigido há ciclo
    // se, e somente se, a busca encontra uma aresta de retorno, isto é, uma aresta
    // u -> v em que v ainda está CINZA (é ancestral de u no caminho atual).
    // aresta para vértice PRETO não indica ciclo: v já foi concluído por outro ramo.
    // a DFS é iterativa (pilha explícita) para não estourar a pilha da JVM nos
    // caminhos longos do grafo da Enron.
    public List<Integer> encontraCiclo() {
        int[] cor = new int[numVertices];      // inicia tudo BRANCO
        int[] pai = new int[numVertices];      // para reconstruir o ciclo
        int[] proxAresta = new int[numVertices]; // próxima aresta a examinar de cada vértice
        ArrayDeque<Integer> pilha = new ArrayDeque<>();

        for (int s = 0; s < numVertices; s++) {
            if (cor[s] != BRANCO) continue;
            cor[s] = CINZA;
            pai[s] = -1;
            pilha.push(s);

            while (!pilha.isEmpty()) {
                int u = pilha.peek();
                List<Aresta> adj = vertices[u].adjacencias;

                if (proxAresta[u] < adj.size()) {
                    int v = adj.get(proxAresta[u]++).destino;
                    if (cor[v] == BRANCO) {
                        cor[v] = CINZA;
                        pai[v] = u;
                        pilha.push(v);
                    } else if (cor[v] == CINZA) {
                        // aresta de retorno u -> v: o ciclo é v -> ... -> u -> v
                        List<Integer> ciclo = new ArrayList<>();
                        for (int w = u; w != v; w = pai[w]) {
                            ciclo.add(w);
                        }
                        ciclo.add(v);
                        Collections.reverse(ciclo);
                        ciclo.add(v);   // fecha o ciclo
                        return ciclo;
                    }
                } else {
                    cor[u] = PRETO;   // todas as arestas de u examinadas
                    pilha.pop();
                }
            }
        }
        return Collections.emptyList();
    }

    public void imprimeCiclo() {
        List<Integer> ciclo = encontraCiclo();
        if (ciclo.isEmpty()) {
            System.out.println("O grafo é ACÍCLICO (não há ciclo dirigido).");
            return;
        }
        System.out.println("O grafo é CÍCLICO. Exemplo de ciclo ("
                + (ciclo.size() - 1) + " aresta(s)):");
        for (int i = 0; i < ciclo.size(); i++) {
            System.out.print(getInformacao(ciclo.get(i)));
            if (i < ciclo.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
    }

    // =====================================================================
    // QUESTÃO 2 - componentes (fracamente conexos)
    // =====================================================================

    // identifica os componentes do grafo ignorando o sentido das arestas: u e v
    // ficam no mesmo componente se houver caminho entre eles quando cada aresta
    // u -> v é tratada como u - v. cada componente é a lista ordenada de índices
    // dos vértices membros; a lista de componentes vem do maior para o menor.
    // complexidade O(V + E): cada vértice entra uma vez na fila e cada aresta é
    // examinada uma vez em cada sentido.
    public List<List<Integer>> componentes() {
        // a lista de adjacência guarda apenas as arestas de saída. para andar no
        // sentido contrário, monta-se uma vez a lista de predecessores (entrada).
        List<List<Integer>> entrada = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            entrada.add(new ArrayList<>());
        }
        for (int u = 0; u < numVertices; u++) {
            for (Aresta a : vertices[u].adjacencias) {
                entrada.get(a.destino).add(u);
            }
        }

        boolean[] visitado = new boolean[numVertices];
        List<List<Integer>> componentes = new ArrayList<>();
        ArrayDeque<Integer> fila = new ArrayDeque<>();

        for (int s = 0; s < numVertices; s++) {
            if (visitado[s]) continue;

            // s não pertence a nenhum componente já encontrado: inicia um novo
            // e uma BFS recolhe todos os vértices alcançáveis sem considerar sentido.
            List<Integer> membros = new ArrayList<>();
            visitado[s] = true;
            fila.add(s);

            while (!fila.isEmpty()) {
                int u = fila.poll();
                membros.add(u);

                for (Aresta a : vertices[u].adjacencias) {   // u -> v
                    if (!visitado[a.destino]) {
                        visitado[a.destino] = true;
                        fila.add(a.destino);
                    }
                }
                for (int v : entrada.get(u)) {                // v -> u
                    if (!visitado[v]) {
                        visitado[v] = true;
                        fila.add(v);
                    }
                }
            }

            Collections.sort(membros);
            componentes.add(membros);
        }

        componentes.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return componentes;
    }

    // resumo: quantidade, distribuição de tamanhos e os membros dos componentes.
    // os componentes com mais de maxMembros vértices são exibidos parcialmente.
    public void imprimeComponentes(int maxComponentes, int maxMembros) {
        List<List<Integer>> comps = componentes();
        System.out.println("\nNúmero de componentes: " + comps.size());
        if (comps.isEmpty()) return;

        System.out.println("Maior componente: " + comps.get(0).size() + " vértices ("
                + String.format("%.2f", 100.0 * comps.get(0).size() / numVertices) + "% do grafo)");

        // tamanho -> quantidade de componentes com esse tamanho
        java.util.TreeMap<Integer, Integer> distribuicao =
                new java.util.TreeMap<>(Collections.reverseOrder());
        for (List<Integer> c : comps) {
            distribuicao.merge(c.size(), 1, Integer::sum);
        }
        System.out.println("Distribuição (tamanho: quantidade de componentes):");
        for (Map.Entry<Integer, Integer> e : distribuicao.entrySet()) {
            System.out.println("  " + e.getKey() + " vértice(s): " + e.getValue());
        }

        int limite = Math.min(maxComponentes, comps.size());
        System.out.println("\nPrimeiros " + limite + " componentes:");
        for (int i = 0; i < limite; i++) {
            List<Integer> c = comps.get(i);
            System.out.print("C" + (i + 1) + " (" + c.size() + "): ");
            for (int k = 0; k < Math.min(maxMembros, c.size()); k++) {
                if (k > 0) System.out.print(", ");
                System.out.print(getInformacao(c.get(k)));
            }
            if (c.size() > maxMembros) System.out.print(", ... (+" + (c.size() - maxMembros) + ")");
            System.out.println();
        }
    }

    // grava todos os componentes, com todos os membros, num arquivo texto.
    public void salvaComponentes(String arquivo) throws java.io.IOException {
        List<List<Integer>> comps = componentes();
        try (java.io.PrintWriter out = new java.io.PrintWriter(
                java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get(arquivo),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            out.println("Total de componentes: " + comps.size());
            for (int i = 0; i < comps.size(); i++) {
                List<Integer> c = comps.get(i);
                out.println();
                out.println("Componente " + (i + 1) + " - " + c.size() + " vértice(s)");
                for (int v : c) {
                    out.println("  " + getInformacao(v));
                }
            }
        }
    }

    // =====================================================================
    // QUESTÃO 3 - verificação de conexidade
    // =====================================================================

    // a partir da questão 2: o grafo é (fracamente) conexo se todos os vértices
    // estão num único componente. por convenção, o grafo vazio é considerado conexo.
    public boolean ehConexo() {
        return componentes().size() <= 1;
    }

    public void imprimeConexidade() {
        int qtd = componentes().size();
        if (qtd <= 1) {
            System.out.println("O grafo é CONEXO (fracamente conexo): 1 componente.");
        } else {
            System.out.println("O grafo é DESCONEXO: " + qtd + " componentes.");
        }
    }

    // =====================================================================
    // AGM - árvore geradora mínima (Kruskal)
    // =====================================================================

    // árvore geradora mínima pelo algoritmo de Kruskal. a AGM é definida para grafos não
    // direcionados, então cada aresta vale como ligação sem sentido. se existirem A->B e B->A,
    // a de menor peso entra e a outra é descartada por formar ciclo. grafo desconexo gera uma
    // floresta (uma árvore por componente). o peso usado é o da aresta: mínimo = vínculos mais fracos.
    public ArvoreGeradoraMinima kruskal() {
        // 1) reúne todas as arestas como {origem, destino, peso}
        List<int[]> todas = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            for (Aresta a : vertices[i].adjacencias) {
                todas.add(new int[]{i, a.destino, a.peso});
            }
        }
        // 2) ordena do menor para o maior peso
        todas.sort((x, y) -> Integer.compare(x[2], y[2]));

        // union-find: pai[v] leva ao representante do conjunto de v. começa cada vértice sozinho.
        int[] pai = new int[numVertices];
        int[] tamanho = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            pai[i] = i;
            tamanho[i] = 1;
        }

        List<int[]> escolhidas = new ArrayList<>();
        long total = 0;
        int componentes = numVertices;   // cada união bem-sucedida reduz um componente

        // 3) da mais leve para a mais pesada: só entra se ligar dois conjuntos diferentes
        for (int[] e : todas) {
            int ra = encontrar(pai, e[0]);
            int rb = encontrar(pai, e[1]);
            if (ra == rb) continue;   // já conectados: a aresta fecharia um ciclo

            // une o conjunto menor ao maior para manter as árvores do union-find rasas
            if (tamanho[ra] < tamanho[rb]) {
                int t = ra;
                ra = rb;
                rb = t;
            }
            pai[rb] = ra;
            tamanho[ra] += tamanho[rb];

            escolhidas.add(e);
            total += e[2];
            componentes--;
        }
        return new ArvoreGeradoraMinima(escolhidas, total, componentes);
    }

    // representante (raiz) do conjunto de v.
    private static int encontrar(int[] pai, int v) {
        int raiz = v;
        while (pai[raiz] != raiz) raiz = pai[raiz];
        // compressão de caminho: quem foi visitado passa a apontar direto para a raiz
        while (pai[v] != raiz) {
            int proximo = pai[v];
            pai[v] = raiz;
            v = proximo;
        }
        return raiz;
    }

    public void imprimeKruskal() {
        ArvoreGeradoraMinima agm = kruskal();
        System.out.println("\nÁrvore Geradora Mínima (Kruskal):");
        System.out.println("Vértices: " + numVertices);
        System.out.println("Componentes conexos: " + agm.componentes);
        System.out.println("Arestas na árvore: " + agm.arestas.size());
        System.out.println("Peso total: " + agm.pesoTotal);

        int mostrar = Math.min(20, agm.arestas.size());
        System.out.println("Primeiras " + mostrar + " arestas escolhidas (da mais leve):");
        for (int i = 0; i < mostrar; i++) {
            int[] e = agm.arestas.get(i);
            System.out.println("  " + getInformacao(e[0]) + " -- " + getInformacao(e[1]) + " (peso " + e[2] + ")");
        }
        if (agm.arestas.size() > mostrar) {
            System.out.println("  ... e mais " + (agm.arestas.size() - mostrar));
        }
    }

    // CARGA DE ARQUIVO PAJEK (.net)

    // lê um arquivo Pajek: *Vertices ('id "rótulo"'), *Edges (sem sentido) e *Arcs (u -> v).
    // *Edges grava os dois sentidos. laços são ignorados; peso ausente vale 1.
    public static GrafoEnron pajekLoader(String arquivo) throws java.io.IOException {
        try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(
                java.nio.file.Paths.get(arquivo), java.nio.charset.StandardCharsets.UTF_8)) {

            GrafoEnron g = null;
            String secao = "";
            String linha;
            while ((linha = r.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("%")) continue;

                if (linha.charAt(0) == '*') {
                    String[] partes = linha.split("\\s+");
                    secao = partes[0].toLowerCase();
                    if (secao.equals("*vertices")) {
                        g = new GrafoEnron(Integer.parseInt(partes[1]));
                    }
                    continue;
                }
                if (g == null) {
                    throw new java.io.IOException("Arquivo Pajek sem a seção *Vertices antes dos dados.");
                }

                if (secao.equals("*vertices")) {
                    // rótulo lido entre aspas (pode ter espaços)
                    int id = Integer.parseInt(linha.split("\\s+", 2)[0]);
                    int ini = linha.indexOf('"');
                    int fim = linha.lastIndexOf('"');
                    String rotulo = (ini >= 0 && fim > ini)
                            ? linha.substring(ini + 1, fim)
                            : String.valueOf(id);
                    g.seta_informacao(id - 1, rotulo);   // ids do Pajek começam em 1
                } else if (secao.equals("*edges") || secao.equals("*arcs")) {
                    String[] t = linha.split("\\s+");
                    int u = Integer.parseInt(t[0]) - 1;
                    int v = Integer.parseInt(t[1]) - 1;
                    int peso = t.length > 2 ? (int) Math.round(Double.parseDouble(t[2])) : 1;
                    if (u == v) continue;
                    g.cria_adjacencia(u, v, peso);
                    if (secao.equals("*edges")) g.cria_adjacencia(v, u, peso);
                }
            }
            if (g == null) {
                throw new java.io.IOException("Arquivo Pajek sem a seção *Vertices.");
            }
            return g;
        }
    }

    // GRAFO NÃO DIRECIONADO - ciclo e coeficiente de agrupamento
    // (supõem arestas simétricas, como as de pajekLoader)

    // cada aresta aparece duas vezes nas listas.
    public int getNumArestasNaoDirecionadas() {
        return getNumArestas() / 2;
    }

    // E - V + C: arestas além da floresta geradora. 0 = acíclico; > 0 = há ciclo.
    // ehCiclico() não serve aqui: u -> v -> u contaria como ciclo.
    public int numeroCiclomatico() {
        return getNumArestasNaoDirecionadas() - numVertices + componentes().size();
    }

    public boolean ehCiclicoNaoDirecionado() {
        return numeroCiclomatico() > 0;
    }

    // coeficiente de agrupamento local: C_i = 2 * T_i / (k_i * (k_i - 1)),
    // T_i = arestas entre os vizinhos de i. grau < 2 -> C_i = 0.
    public double[] coeficientesAgrupamento() {
        List<java.util.Set<Integer>> vizinhos = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            java.util.Set<Integer> s = new java.util.HashSet<>();
            for (Aresta a : vertices[i].adjacencias) {
                if (a.destino != i) s.add(a.destino);
            }
            vizinhos.add(s);
        }

        double[] c = new double[numVertices];
        for (int i = 0; i < numVertices; i++) {
            int k = vizinhos.get(i).size();
            if (k < 2) continue;

            // u < v conta cada par uma vez
            long triangulos = 0;
            for (int u : vizinhos.get(i)) {
                java.util.Set<Integer> vu = vizinhos.get(u);
                for (int v : vizinhos.get(i)) {
                    if (u < v && vu.contains(v)) triangulos++;
                }
            }
            c[i] = 2.0 * triangulos / ((double) k * (k - 1));
        }
        return c;
    }

    // média dos coeficientes: sobre todos os vértices (true) ou só os de grau >= 2 (false).
    public double coeficienteMedio(double[] c, boolean incluirGrauMenorQue2) {
        double soma = 0;
        int n = 0;
        for (int i = 0; i < numVertices; i++) {
            if (!incluirGrauMenorQue2 && vertices[i].adjacencias.size() < 2) continue;
            soma += c[i];
            n++;
        }
        return n == 0 ? 0 : soma / n;
    }

    // n vértices de maior coeficiente; empate: maior grau, depois menor índice.
    public List<Integer> topAgrupamento(double[] c, int n) {
        List<Integer> idx = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) idx.add(i);
        idx.sort((a, b) -> {
            int cmp = Double.compare(c[b], c[a]);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(vertices[b].adjacencias.size(), vertices[a].adjacencias.size());
            return cmp != 0 ? cmp : Integer.compare(a, b);
        });
        return idx.subList(0, Math.min(n, numVertices));
    }

    public int grau(int i) {
        return verticeValido(i) ? vertices[i].adjacencias.size() : 0;
    }

    // =====================================================================
    // TDE 4 - componentes, clique e clique maximal
    // =====================================================================

    // retorna a quantidade de componentes e imprime os vértices de cada um.
    // em grafo direcionado são considerados os componentes FRACAMENTE conexos
    // (o sentido das arestas é ignorado), reaproveitando componentes().
    public int encontraComponentes() {
        List<List<Integer>> comps = componentes();
        System.out.println("Quantidade de componentes: " + comps.size());
        for (int i = 0; i < comps.size(); i++) {
            List<Integer> c = comps.get(i);
            System.out.print("Componente " + (i + 1) + " (" + c.size() + " vértice(s)): ");
            for (int k = 0; k < c.size(); k++) {
                if (k > 0) System.out.print(", ");
                System.out.print(rotuloOuIndice(c.get(k)));
            }
            System.out.println();
        }
        return comps.size();
    }

    // V forma um clique se todo par de vértices distintos de V é adjacente.
    // como o grafo é direcionado, u e v são adjacentes se existir u -> v OU v -> u
    // (mesmo critério dos componentes fracamente conexos). vértices repetidos em V
    // são considerados uma única vez; índice inválido -> false. o conjunto vazio e
    // o conjunto unitário são cliques (não há par a verificar).
    public boolean eClique(java.util.Collection<Integer> V) {
        if (V == null) return false;
        java.util.Set<Integer> conjunto = new java.util.LinkedHashSet<>(V);
        for (int v : conjunto) {
            if (!verticeValido(v)) return false;
        }
        List<Integer> lista = new ArrayList<>(conjunto);
        for (int a = 0; a < lista.size(); a++) {
            for (int b = a + 1; b < lista.size(); b++) {
                if (!adjacentesSemSentido(lista.get(a), lista.get(b))) return false;
            }
        }
        return true;
    }

    // V é um clique maximal se é clique e nenhum vértice fora de V é adjacente
    // a todos os vértices de V (senão ele poderia ser incluído, aumentando o clique).
    // basta testar os vizinhos de um vértice qualquer de V: quem não é vizinho dele
    // já não pode estender o clique.
    public boolean eMaximal(java.util.Collection<Integer> V) {
        if (!eClique(V)) return false;
        java.util.Set<Integer> conjunto = new java.util.HashSet<>(V);

        // clique vazio só é maximal se o grafo não tiver vértices.
        if (conjunto.isEmpty()) return numVertices == 0;

        int v0 = conjunto.iterator().next();
        for (int w : vizinhosSemSentido(v0)) {
            if (conjunto.contains(w)) continue;
            boolean ligadoATodos = true;
            for (int v : conjunto) {
                if (!adjacentesSemSentido(w, v)) {
                    ligadoATodos = false;
                    break;
                }
            }
            if (ligadoATodos) return false;   // w estende o clique: V não é maximal
        }
        return true;
    }

    // versões que recebem os índices como vetor: eClique(new int[]{0, 1, 2}).
    public boolean eClique(int[] V) {
        return V != null && eClique(paraLista(V));
    }

    public boolean eMaximal(int[] V) {
        return V != null && eMaximal(paraLista(V));
    }

    private static List<Integer> paraLista(int[] V) {
        List<Integer> l = new ArrayList<>(V.length);
        for (int v : V) l.add(v);
        return l;
    }

    // existe aresta u -> v ou v -> u (laços u == v não contam).
    private boolean adjacentesSemSentido(int u, int v) {
        if (u == v) return false;
        return temAresta(u, v) || temAresta(v, u);
    }

    private boolean temAresta(int i, int j) {
        for (Aresta a : vertices[i].adjacencias) {
            if (a.destino == j) return true;
        }
        return false;
    }

    // vizinhos de v ignorando o sentido: destinos das arestas de saída de v e
    // origens das arestas que chegam em v (essas exigem varrer todas as listas).
    private java.util.Set<Integer> vizinhosSemSentido(int v) {
        java.util.Set<Integer> viz = new java.util.HashSet<>();
        for (Aresta a : vertices[v].adjacencias) {
            if (a.destino != v) viz.add(a.destino);
        }
        for (int u = 0; u < numVertices; u++) {
            if (u != v && temAresta(u, v)) viz.add(u);
        }
        return viz;
    }

    private String rotuloOuIndice(int v) {
        String r = vertices[v].informacao;
        return (r == null || r.isEmpty()) ? String.valueOf(v) : r;
    }
}
