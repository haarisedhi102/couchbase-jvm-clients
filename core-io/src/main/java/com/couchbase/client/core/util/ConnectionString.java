/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.util;

import com.couchbase.client.core.error.CouchbaseException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a {@link ConnectionString}.
 *
 * @author Michael Nitschinger
 * @since 2.4.0
 */
public class ConnectionString {

    public static final String DEFAULT_SCHEME = "couchbase://";

    private final Scheme scheme;
    private final List<InetSocketAddress> hosts;
    private final List<InetSocketAddress> allHosts;
    private final Map<String, String> params;
    private final String username;

    protected ConnectionString(final String input) {
        if (input.contains("://")) {
            this.scheme = parseScheme(input);
        } else {
            this.scheme = Scheme.COUCHBASE;
        }

        this.username = parseUser(input);
        this.allHosts = parseHosts(input);
        this.hosts = tryResolveHosts(this.allHosts);
        this.params = parseParams(input);
    }

    public static ConnectionString create(final String input) {
        return new ConnectionString(input);
    }

    public static ConnectionString fromHostnames(final List<String> hostnames) {
        StringBuilder sb = new StringBuilder(DEFAULT_SCHEME);
        for (int i = 0; i < hostnames.size(); i++) {
            sb.append(hostnames.get(i));
            if (i < hostnames.size()-1) {
                sb.append(",");
            }
        }
        return create(sb.toString());
    }

    static Scheme parseScheme(final String input) {
        if (input.startsWith("couchbase://")) {
            return Scheme.COUCHBASE;
        } else if (input.startsWith("couchbases://")) {
            return Scheme.COUCHBASES;
        } else if (input.startsWith("http://")) {
            return Scheme.HTTP;
        } else {
            throw new CouchbaseException("Could not parse Scheme of connection string: " + input);
        }
    }

    static String parseUser(final String input) {
        if (!input.contains("@")) {
            return null;
        } else {
            String schemeRemoved = input.replaceAll("\\w+://", "");
            String username = schemeRemoved.replaceAll("@.*", "");
            return username;
        }
    }

    static List<InetSocketAddress> parseHosts(final String input) {
        String schemeRemoved = input.replaceAll("\\w+://", "");
        String usernameRemoved = schemeRemoved.replaceAll(".*@", "");
        String paramsRemoved = usernameRemoved.replaceAll("\\?.*", "");
        String[] splitted = paramsRemoved.split(",");

        List<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();

        Pattern ipv6pattern = Pattern.compile("^\\[(.+)]:(\\d+)$");
        for (int i = 0; i < splitted.length; i++) {
            String singleHost = splitted[i];
            if (singleHost == null || singleHost.isEmpty()) {
                continue;
            }
            singleHost = singleHost.trim();

            Matcher matcher = ipv6pattern.matcher(singleHost);
            if (singleHost.startsWith("[") && singleHost.endsWith("]")) {
                // this is an ipv6 addr!
                singleHost = singleHost.substring(1, singleHost.length() - 1);
                hosts.add(new InetSocketAddress(singleHost, 0));
            } else if (matcher.matches()) {
                // this is ipv6 with addr and port!
                hosts.add(new InetSocketAddress(
                    matcher.group(1),
                    Integer.parseInt(matcher.group(2)))
                );
            } else {
                // either ipv4 or a hostname
                String[] parts = singleHost.split(":");
                if (parts.length == 1) {
                    hosts.add(new InetSocketAddress(parts[0], 0));
                } else {
                    hosts.add(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
                }
            }
        }
        return hosts;
    }

    //Make sure the address is resolvable
    static List<InetSocketAddress> tryResolveHosts(final List<InetSocketAddress> hosts) {
        List<InetSocketAddress> resolvableHosts = new ArrayList<InetSocketAddress>();
        for (InetSocketAddress node : hosts) {
            try {
                node.getAddress().getHostAddress();
                resolvableHosts.add(node);
            } catch (NullPointerException ex) {
                // todo: LOGGER.error("Unable to resolve address " + node.toString());
            }
        }
        return resolvableHosts;
    }

    static Map<String, String> parseParams(final String input) {
        try {
            String[] parts = input.split("\\?");
            Map<String, String> params = new HashMap<String, String>();
            if (parts.length > 1) {
                String found = parts[1];
                String[] exploded = found.split("&");
                for (int i = 0; i < exploded.length; i++) {
                    String[] pair = exploded[i].split("=");
                    params.put(pair[0], pair[1]);
                }
            }
            return params;
        } catch(Exception ex) {
            throw new CouchbaseException("Could not parse Params of connection string: " + input, ex);
        }
    }

    public Scheme scheme() {
        return scheme;
    }

    public String username() {
        return username;
    }

    /**
     * Get the list of hosts that could be resolved
     *
     * @return hosts
     */
    public List<InetSocketAddress> hosts() {
        return hosts;
    }

    public Map<String, String> params() {
        return params;
    }

    /**
     * Get the list of all hosts set on the connection string.
     *
     * @return hosts
     */
    public List<InetSocketAddress> allHosts() {
        return allHosts;
    }

    public enum Scheme {
        HTTP,
        COUCHBASE,
        COUCHBASES
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionString{");
        sb.append("scheme=").append(scheme);
        sb.append(", user=").append(username);
        sb.append(", hosts=").append(hosts);
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
