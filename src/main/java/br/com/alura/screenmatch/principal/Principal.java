package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodios;
import br.com.alura.screenmatch.services.ConsumoApi;
import br.com.alura.screenmatch.services.ConverteDados;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=9a3513ec";

    private  Scanner scanner = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu(){
        System.out.println("Digite o nome da série para pesquisar: ");
        var nomeSerie = scanner.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(' ', '+') + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
        for(int i = 1; i <= dados.totalTemporadas(); i++){
            json = consumoApi.obterDados(ENDERECO+nomeSerie.replace(' ', '+') + "&season="+ i +API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

//        for(int i = 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for(int j = 0; j < episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        temporadas.forEach(t->t.episodios().forEach(e-> System.out.println(e.titulo())));

        //iterando com streams sob as temporadas, acessando a sua lista de episódios e criando um stream com esses episodios para gerar nova lista
        List<DadosEpisodio> dadosEpisodios =
                temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                        .collect(Collectors.toList());

//        System.out.println("\nTop 10 episódios: ");
//        dadosEpisodios.stream()
//                .filter(f -> !f.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(a -> System.out.println("Filtro: " + a))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(a -> System.out.println("Ordenação: " + a))
//                .limit(10)
//                .peek(a -> System.out.println("Limite: " + a))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(a -> System.out.println("Mapeamento: " + a))
//                .forEach(System.out::println);

        List<Episodios> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodios(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite um trecho do título do episódio: ");
//        var trechoTitulo = scanner.nextLine();
//        Optional<Episodios> episodioBuscado = episodios.stream()
//                .filter(f -> f.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//
//        if(episodioBuscado.isPresent()){
//            System.out.println("Episódio encontrado! ");
//            System.out.println("Temporada " + episodioBuscado.get().getTemporada());
//        } else {
//            System.out.println("Episódio não encontrado! :(");
//        }
//
//        System.out.println("A partir de que ano você quer ver os episódios? ");
//        int ano = scanner.nextInt();
//        scanner.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getTitulo() +
//                                " Data lançamento: " + e.getDataLancamento().format(formatador)
//                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(
                Collectors
                        .groupingBy(
                                Episodios::getTemporada,
                                Collectors.averagingDouble(Episodios::getAvaliacao)
                        )
        );

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodios::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());
    }
}
