/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.generetor.webserviceclient.resource.Api;

import com.smartitengineering.generetor.webserviceclient.domain.Api.ReportConfig;
import com.smartitengineering.util.rest.client.WritableResource;
import org.apache.abdera.model.Feed;
import java.util.Collection;

/**
 *
 * @author saumitra
 */
public interface ConfigsResource extends WritableResource<Feed> {

  public ConfigsResource createConfig(ReportConfig reportConfig);

  public Collection<ConfigResource> searchConfig();
}
