// tag::comment[]
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::comment[]
package io.openliberty.guides.microprofile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.openliberty.guides.microprofile.util.InventoryUtil;
import io.openliberty.guides.microprofile.util.JsonMessages;

@ApplicationScoped
public class InventoryManager {

  private ConcurrentMap<String, JsonObject> inv = new ConcurrentHashMap<>();

  public JsonObject get(String hostname) {
    JsonObject properties = inv.get(hostname);
    if (properties == null) {
      if (InventoryUtil.responseOk(hostname)) {
        properties = InventoryUtil.getProperties(hostname);
        this.add(hostname, properties);
      } else {
        return JsonMessages.SERVICE_UNREACHABLE.getJson();
      }
    }
    return properties;
  }

  public void add(String hostname, JsonObject systemProps) {
    inv.putIfAbsent(hostname, systemProps);
  }

  public JsonObject list() {
    JsonObjectBuilder systems = Json.createObjectBuilder();
    inv.forEach((host, props) -> {
      JsonObject systemProps = Json.createObjectBuilder()
          .add("os.name", props.getString("os.name"))
          .add("user.name", props.getString("user.name")).build();
      systems.add(host, systemProps);
    });
    systems.add("hosts", systems);
    systems.add("total", inv.size());
    return systems.build();
  }
}
