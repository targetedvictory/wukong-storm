package com.infochimps.wukong.storm;

import java.io.File;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;

import com.infochimps.wukong.storm.WukongTopologyBuilder;

public class WukongTopologySubmitter {

    private static Logger LOG = Logger.getLogger(WukongTopologySubmitter.class);
    private WukongTopologyBuilder builder;

    public static void main(String[] args) throws Exception {
	setPropertiesFromArgsBecauseStupidlyHard(args);
	WukongTopologySubmitter submitter = new WukongTopologySubmitter();
	submitter.validate();
	submitter.submit();
	System.exit(0);
    }

    public static void setPropertiesFromArgsBecauseStupidlyHard(String[] args) {
	int numArgs      = args.length;
	int argIndex     = 0;
	boolean isOption = false;
	while (argIndex < numArgs) {
	    String arg = args[argIndex];
	    if (isOption) {
		setPropertyFromArgBecauseStupidlyHard(arg);
		isOption = false;
	    } else {
		if (arg.matches("-D.+")) {
		    setPropertyFromArgBecauseStupidlyHard(arg.substring(2));
		} else if (arg.matches("-D")) {
		    isOption = true;
		} else {
		    LOG.error("Malformed option: " + arg);
		}
	    }
	    argIndex += 1;
	}
    }

    private static void setPropertyFromArgBecauseStupidlyHard(String arg) {
	String[] parts = arg.split("=");
	if (parts.length >= 2) {
	    String key   = parts[0];
	    String value = arg.substring(key.length() + 1);
	    System.setProperty(key, value);
	} else {
	    LOG.error("Invalid property: " + arg);
	}
    }

    public WukongTopologySubmitter() {
	this.builder = new WukongTopologyBuilder();
    }

    private void validate() {
	if (!builder.valid()) {
	    System.out.println(usage());
	    System.exit(1);
	}
    }

    private String usage() {
	return "usage: storm jar " + fullyQualifiedClassPath() + " " + WukongTopologyBuilder.usageArgs();
    }
    
    private File fullyQualifiedClassPath() {
	return new File(WukongTopologySubmitter.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }
    
    private Config config() {
	return new Config();
    }

    private void submit() {
	try {
	    StormSubmitter.submitTopology(builder.topologyName(), config(), builder.topology());
	} catch (AlreadyAliveException e) {
	    LOG.error("Topology " + builder.topologyName() + " is already running", e);
	    System.exit(2);
	} catch (InvalidTopologyException e) {
	    LOG.error("Topology " + builder.topologyName() + " is invalid", e);
	    System.exit(3);
	}
    }
}