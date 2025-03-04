package com.lca.analyzerlcaspring.config;

import com.lca.analyzerlcaspring.service.AssignmentServiceImpl;
import com.lca.core.CustomAnalyzer;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;


@Configuration
public class CustomAnalyzerConfig {
    @Bean
    public List<CustomAnalyzer> customAnalyzerList(
            AutowireCapableBeanFactory beanFactory
    ) {
        List<CustomAnalyzer> customAnalyzerList = new ArrayList<>();
        List<URL> analyzerURLs = new ArrayList<>();
        File folder = new File("../../backend-files/");
        if (folder.listFiles() != null) {
            List<File> analyzers = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));
            for (File analyzer : analyzers) {
                if (FilenameUtils.getExtension(analyzer.getName()).equals("jar")) {
                    try {
                        URL analyzerUrl = new URL(MessageFormat.format("jar:{0}!/", analyzer.toURI().toURL()));
                        analyzerURLs.add(analyzerUrl);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(analyzerURLs)
                        .setScanners(
                                TypesAnnotated,
                                SubTypes,
                                Scanners.ConstructorsSignature,
                                Scanners.Resources
                        )
        );

        URLClassLoader[] loaders = new URLClassLoader[analyzerURLs.size()];
        for (int i = 0; i < analyzerURLs.size(); i++) {
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{analyzerURLs.get(i)},
                    this.getClass().getClassLoader()
            );

            loaders[i] = loader;
        }

        Set<Class<?>> rs = reflections.get(SubTypes.of(CustomAnalyzer.class).asClass(loaders));
        for (Class<?> aClass : rs) {
            customAnalyzerList.add((CustomAnalyzer) beanFactory.createBean(aClass));
        }

        return customAnalyzerList;
    }
}
