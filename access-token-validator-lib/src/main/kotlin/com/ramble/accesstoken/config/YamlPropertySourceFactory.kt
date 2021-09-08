package com.ramble.accesstoken.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory

class YamlPropertySourceFactory : PropertySourceFactory {

    override fun createPropertySource(name: String?, encodedResource: EncodedResource): PropertySource<*> {
        val yamlFactory = YamlPropertiesFactoryBean()
        yamlFactory.setResources(encodedResource.resource)
        val properties = yamlFactory.getObject()
        // TODO: Use logger for encodedResource.resource.filename and properties
        return PropertiesPropertySource(encodedResource.resource.filename!!, properties!!)
    }

}