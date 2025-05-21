package org.xiaoyu;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.xiaoyu.api.DatabaseService;
import org.xiaoyu.api.WeatherService;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService openMeteoService, DatabaseService databaseService) {
		return MethodToolCallbackProvider.builder().toolObjects(openMeteoService,databaseService).build();
	}

}