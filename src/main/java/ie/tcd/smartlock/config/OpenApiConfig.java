package ie.tcd.smartlock.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xylingying
 * @date 2025-03-07 0:41
 * @description: Swagger启用JWT
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IoT 接口文档")
                        .version("v1.1")
                        .description("智能门锁后端 API")
                )
                .components(new Components().addSecuritySchemes("BearerToken",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                )
                // 让所有接口都"挂锁"
                .addSecurityItem(new SecurityRequirement().addList("BearerToken")
                );
    }
}
