package com.ramble.email.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory

class YamlPropertySourceFactory : PropertySourceFactory {

    private val logger = LoggerFactory.getLogger(YamlPropertySourceFactory::class.java)

    override fun createPropertySource(name: String?, encodedResource: EncodedResource): PropertySource<*> {
        val yamlFactory = YamlPropertiesFactoryBean()
        yamlFactory.setResources(encodedResource.resource)
        val properties = yamlFactory.getObject()
        logger.info("email config filename:${encodedResource.resource.filename} properties:$properties")
        if (encodedResource.resource.filename == null || properties == null) {
            logger.error("email config has null values. filename:${encodedResource.resource.filename} properties:$properties")
        }
        return PropertiesPropertySource(encodedResource.resource.filename!!, properties!!)
    }

}