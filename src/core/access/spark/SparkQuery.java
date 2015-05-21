package core.access.spark;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import core.access.Predicate;
import core.access.iterator.IteratorRecord;
import core.utils.ConfUtils;

public class SparkQuery {

	private SparkQueryConf queryConf;
	private JavaSparkContext ctx;

	private Predicate[] predicates;

	private ConfUtils cfg;

	public SparkQuery(ConfUtils config) {
		this.cfg = config;
		//ctx = new JavaSparkContext(cfg.getSPARK_MASTER(), this.getClass().getName());


		SparkConf sconf = new SparkConf()
								.setMaster(cfg.getSPARK_MASTER())
								.setAppName(this.getClass().getName())
								.setSparkHome(cfg.getSPARK_HOME())
								.setJars(new String[]{cfg.getSPARK_JAR()})
								.set("spark.hadoop.cloneConf", "false")
								.set("spark.executor.memory", "4g");

		ctx = new JavaSparkContext(sconf);
		//ctx = new JavaSparkContext(cfg.getSPARK_MASTER(), this.getClass().getName(), cfg.getSPARK_HOME(), cfg.getSPARK_JAR());
		queryConf = new SparkQueryConf(ctx.hadoopConfiguration());
	}

	public void createTextFile(String localPath, Predicate... ps){
		JavaRDD<String> distFile = ctx.textFile(localPath);
		long lines = distFile.count();
		System.out.println("Number of lines = "+lines);
	}

	public void createHadoopFile(String hdfsPath, Predicate... ps){
		JavaPairRDD<LongWritable,Text> distFile = ctx.hadoopFile(hdfsPath, TextInputFormat.class, LongWritable.class, Text.class);
		long lines = distFile.count();
		System.out.println("Number of lines = "+lines);
	}

	public void createHadoopRDD(String hdfsPath, Predicate... ps){
		JobConf conf = new JobConf(ctx.hadoopConfiguration());
		FileInputFormat.setInputPaths(conf, hdfsPath);

		JavaPairRDD<LongWritable,Text> distFile = ctx.hadoopRDD(conf, TextInputFormat.class, LongWritable.class, Text.class);
		long lines = distFile.count();
		System.out.println("Number of lines = "+lines);
	}

	public void createNewAPIHadoopRDD(String hdfsPath, Predicate... ps){

		queryConf.setDataset(hdfsPath);
		queryConf.setPredicates(predicates);
		queryConf.setWorkers(cfg.getNUM_RACKS() * cfg.getNODES_PER_RACK() * cfg.getMAP_TASKS());
		queryConf.setHadoopHome(cfg.getHADOOP_HOME());
		queryConf.setZookeeperHosts(cfg.getZOOKEEPER_HOSTS());
		queryConf.setMaxSplitSize(1024 / 64);	// number of 64 MB partitions that can fit for each worker (we assume 1GB memory for each worker)

		JavaPairRDD<LongWritable,Text> distFile = ctx.newAPIHadoopFile(
				cfg.getHADOOP_NAMENODE() +  hdfsPath,
				org.apache.hadoop.mapreduce.lib.input.TextInputFormat.class,
				LongWritable.class,
				Text.class,
				ctx.hadoopConfiguration()
			);

		//JavaPairRDD<LongWritable,Text> distFile = ctx.hadoopRDD(conf, TextInputFormat.class, LongWritable.class, Text.class);
		long lines = distFile.count();
		System.out.println("Number of lines = "+lines);
	}


	public JavaPairRDD<LongWritable,IteratorRecord> createRDD(String hdfsPath, Predicate... ps){
		queryConf.setDataset(hdfsPath);
		queryConf.setPredicates(ps);
		queryConf.setWorkers(cfg.getNUM_RACKS() * cfg.getNODES_PER_RACK() * cfg.getMAP_TASKS());
		queryConf.setHadoopHome(cfg.getHADOOP_HOME());
		queryConf.setZookeeperHosts(cfg.getZOOKEEPER_HOSTS());
		queryConf.setMaxSplitSize(1024 / 64);	// number of 64 MB partitions that can fit for each worker (we assume 1GB memory for each worker)
		// ctx.hadoopConfiguration().setClass(FileInputFormat.PATHFILTER_CLASS, SparkPathFilter.class, PathFilter.class);

//		System.setProperty("spark.executor.memory","4g");
//		System.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

		return ctx.newAPIHadoopFile(
				cfg.getHADOOP_NAMENODE() +  hdfsPath,
				SparkInputFormat.class,
				LongWritable.class,
				IteratorRecord.class,
				ctx.hadoopConfiguration()
			);
	}
}
