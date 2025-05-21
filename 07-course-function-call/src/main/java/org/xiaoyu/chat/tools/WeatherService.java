package org.xiaoyu.chat.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 利用OpenMeteo的免费天气API提供天气服务
 * 该API无需API密钥，可以直接使用
 */
@Slf4j
public class WeatherService {

    // OpenMeteo免费天气API基础URL
    private static final String BASE_URL = "https://api.open-meteo.com/v1";

    private final RestClient restClient;

    public WeatherService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "OpenMeteoClient/1.0")
                .build();
    }


    /**
     * 获取指定位置的空气质量信息 (使用备用模拟数据)
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 空气质量信息
     */
    @Tool(name = "getAirQuality", description = "获取指定位置的空气质量信息",returnDirect = true)
    public String getAirQuality(@ToolParam(description = "纬度") double latitude,
                                @ToolParam(description = "经度") double longitude
//                                ,ToolContext context
                                ) {
        log.info("latitude:{}", latitude);
//        log.info("ToolContext:latitude:{}", context.getContext().get("latitude"));
        // 模拟空气质量数据 - 实际情况下应该从真实API获取
        int europeanAqi = (int) (Math.random() * 100) + 1;
        int usAqi = (int) (europeanAqi * 1.5);
        double pm10 = Math.random() * 50 + 5;
        double pm25 = Math.random() * 25 + 2;
        double co = Math.random() * 500 + 100;
        double no2 = Math.random() * 40 + 5;
        double so2 = Math.random() * 20 + 1;
        double o3 = Math.random() * 80 + 20;

        String aqiLevel = getAqiLevel(europeanAqi);
        String usAqiLevel = getUsAqiLevel(usAqi);

        // 构建空气质量信息字符串
        String aqiInfo = String.format("""
                        空气质量信息 (纬度: %.4f, 经度: %.4f):
                        
                        欧洲空气质量指数 (EAQI): %d (%s)
                        美国空气质量指数 (US AQI): %d (%s)
                        
                        详细污染物信息:
                        PM10: %.1f μg/m³
                        PM2.5: %.1f μg/m³
                        一氧化碳 (CO): %.1f μg/m³
                        二氧化氮 (NO2): %.1f μg/m³
                        二氧化硫 (SO2): %.1f μg/m³
                        臭氧 (O3): %.1f μg/m³
                        
                        注意：以上是模拟数据，仅供示例。
                        """,
                latitude, longitude,
                europeanAqi, aqiLevel,
                usAqi, usAqiLevel,
                pm10, pm25, co, no2, so2, o3);
        return aqiInfo;
    }

    /**
     * 获取欧洲AQI等级描述
     */
    private String getAqiLevel(Integer aqi) {
        if (aqi <= 20) {
            return "优 (0-20): 空气质量非常好";
        } else if (aqi <= 40) {
            return "良 (20-40): 空气质量良好";
        } else if (aqi <= 60) {
            return "中等 (40-60): 对敏感人群可能有影响";
        } else if (aqi <= 80) {
            return "较差 (60-80): 对所有人群健康有影响";
        } else if (aqi <= 100) {
            return "差 (80-100): 可能对所有人群健康造成损害";
        } else {
            return "非常差 (>100): 对所有人群健康有严重影响";
        }
    }

    /**
     * 获取美国AQI等级描述
     */
    private String getUsAqiLevel(Integer aqi) {
        if (aqi <= 50) {
            return "优 (0-50): 空气质量令人满意，污染风险很低";
        } else if (aqi <= 100) {
            return "良 (51-100): 空气质量尚可，对极少数敏感人群可能有影响";
        } else if (aqi <= 150) {
            return "对敏感人群不健康 (101-150): 敏感人群可能会经历健康影响";
        } else if (aqi <= 200) {
            return "不健康 (151-200): 所有人可能开始经历健康影响";
        } else if (aqi <= 300) {
            return "非常不健康 (201-300): 健康警告，所有人可能经历更严重的健康影响";
        } else {
            return "危险 (>300): 健康警报，所有人更可能受到影响";
        }
    }
}
