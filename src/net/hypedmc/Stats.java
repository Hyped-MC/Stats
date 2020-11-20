package net.hypedmc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.OptionalDouble;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

public class Stats extends JavaPlugin {

	private static String init_date;
	private static String end_date;

	private static final Map<String, Integer> onlines_stats = Maps.newHashMap();

	private static int tryCount = 1;

	public void saveStats() {
		try {
			File file = new File(getDataFolder(), String.format("%s.html",
					DateTimeFormatter.ofPattern("yyyy-MM-dd-" + tryCount).format(LocalDateTime.now())));
			if (!file.exists())
				file.createNewFile();
			else {
				tryCount += 1;
				saveStats();
				return;
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(getInfos());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEnable() {
		init_date = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());

		if (!getDataFolder().exists())
			getDataFolder().mkdirs();

		getServer().getPluginManager().registerEvents(new Listener() {

			@EventHandler
			private void onJoin(PlayerJoinEvent event) {
				onlines_stats.put(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()),
						Bukkit.getOnlinePlayers().size());
			}

			@EventHandler
			private void onQuit(PlayerQuitEvent event) {
				onlines_stats.put(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()),
						Bukkit.getOnlinePlayers().size());
			}

		}, this);
	}

	@Override
	public void onDisable() {
		end_date = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
		saveStats();
	}

	public static String getInfos() {
		int max = 0;
		int min = 0;
		double average = 0;

		for (int onlines : onlines_stats.values()) {
			max = Math.max(max, onlines);
			min = Math.min(min, onlines);
		}
		OptionalDouble optionalDouble = onlines_stats.values().stream().mapToDouble(a -> a).average();

		if (optionalDouble.isPresent()) {
			average = optionalDouble.getAsDouble();
		}
		String result = "";
		for (String date : onlines_stats.keySet()) {
			result += String.format("['%s',%d],", date, onlines_stats.get(date));
		}
		if (result.endsWith(","))
			result = result.substring(0, result.length() - 1);

		return String.format("<html>\r\n" + "<head>\r\n" + "	<title>Estatistica de Jogadores Onlines</title>\r\n"
				+ "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\r\n"
				+ "    <script>\r\n" + "        google.charts.load('current', {'packages':['corechart']});\r\n"
				+ "        function showChart() {\r\n"
				+ "            var tabela = new google.visualization.DataTable();\r\n"
				+ "            tabela.addColumn('string','horario');\r\n"
				+ "            tabela.addColumn('number','onlines');\r\n" + "            tabela.addRows([\r\n"
				+ "               " + result + "\r\n" + "            ]);\r\n"
				+ "            var grafico = new google.visualization.AreaChart(document.getElementById('grafico'));\r\n"
				+ "            grafico.draw(tabela);\r\n" + "        }\r\n"
				+ "        google.charts.setOnLoadCallback(showChart);\r\n" + "    </script>\r\n" + "</head>\r\n"
				+ "<body>\r\n" + "	<h1>Estatistica de jogadores onlines de %s ate %s<h1>\r\n"
				+ "    <div id=\"grafico\"></div></br>\r\n" + "	<span>Maximo de Jogadores Onlines: %d<span></br>\r\n"
				+ "	<span>Minimo de Jogadores Onlines: %d<span></br>\r\n"
				+ "	<span>Media de Jogadores Onlines: %s<span></br>\r\n" + "</body>\r\n" + "</html>", init_date,
				end_date, max, min, new DecimalFormat("##.#").format(average));
	}
	
}
