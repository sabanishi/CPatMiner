package main;

import java.io.File;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;

import utils.FileIO;
import utils.NotifyingBlockingThreadPoolExecutor;
import change.ChangeAnalyzer;

public class MainChangeAnalyzer {
	private static int THREAD_POOL_SIZE = 1;

	private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			return true; // keep waiting
		}
	};
	private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

	public static String inputPath = "/Users/sakugawa99/WebGL/CPatMiner/repos";
	public static String outputPath = "/Users/sakugawa99/WebGL/CPatMiner/output";
	
	public static void main(String[] args) {
		String content = null;

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-i")) {
					inputPath = args[i+1];
				}
				if (args[i].equals("-o")) {
					outputPath = args[i+1];
				}
			}
		}

		THREAD_POOL_SIZE = 8;
		pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);
		Path path = Path.of("../repos/list.csv");
		content = FileIO.readStringFromFile(path.toString());

		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			int index = line.indexOf(',');
			if (index < 0)
				index = line.length();
			String name = line.substring(0, index);
			File dir = new File(inputPath + "/" + name);
			analyze(dir, name);
		}
		sc.close();
		try {
			pool.await(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (final InterruptedException e) { }
	}

	private static void analyze(final File dir, final String name) {
		if (!dir.isDirectory())
			return;
		File git = new File(dir, ".git");
		if (git.exists()) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					long startProjectTime = System.currentTimeMillis();
					System.out.println(name);
					String url = dir.getAbsolutePath();
					ChangeAnalyzer ca = new ChangeAnalyzer(name, -1, url);
					ca.buildGitConnector();
					ca.analyzeGit();
			    	long endProjectTime = System.currentTimeMillis();
			    	ca.getCproject().setRunningTime(endProjectTime - startProjectTime);
			    	ca.closeGitConnector();
					System.out.println("Done " + name + " in " + (endProjectTime - startProjectTime) / 1000 + "s");
				}
			});
		}
	}

}
