package org.id.bankspringbatch;



import org.id.bankspringbatch.dao.BankTransaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private ItemReader<BankTransaction> bankTransactionItemReader;
	@Autowired private ItemWriter<BankTransaction> bankTransactionItemWriter;
	@Autowired private ItemProcessor<BankTransaction,BankTransaction> bankTransactionItemProcessor;
	
	@Bean
	public Job myJob() {
		Step step = stepBuilderFactory.get("step-load-data")
				.<BankTransaction,BankTransaction>chunk(100)
				.reader(bankTransactionItemReader)				
				.writer(bankTransactionItemWriter)
				.processor(bankTransactionItemProcessor)
				.build();
		
		return jobBuilderFactory.get("bank-data-loader-job")
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
	}
	
	@Bean
	public ItemReader<BankTransaction> flatFileItemReader(@Value("${inputFile}") Resource inputFile) {
		FlatFileItemReader<BankTransaction> CSVItemReader = new FlatFileItemReader<>();
		CSVItemReader.setName("CSV-Reader");
		CSVItemReader.setLinesToSkip(1);
		CSVItemReader.setResource(inputFile);
		CSVItemReader.setLineMapper(lineMapper());
		return CSVItemReader;
	}
	
	
	

	public LineMapper<BankTransaction> lineMapper() {
		DefaultLineMapper<BankTransaction> lineMapper = new DefaultLineMapper<>();
		
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id","accountId","strTransactionDate","transactionType","amount");
		
		lineMapper.setLineTokenizer(lineTokenizer);
		
		BeanWrapperFieldSetMapper<BankTransaction> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(BankTransaction.class);
		
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		return lineMapper;
	}
	


}
