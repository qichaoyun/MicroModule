package com.eastwood.tools.plugins.core

import org.gradle.api.GradleException
import org.gradle.api.Project

class MicroModuleInfo {

    Project project
    MicroModule mainMicroModule
    List<MicroModule> includeMicroModules

    Map<String, List<String>> microModuleDependency

    Digraph<String> dependencyGraph

    MicroModuleInfo(Project project) {
        this.project = project
        this.includeMicroModules = new ArrayList<>()
        microModuleDependency = new HashMap<>()
        dependencyGraph = new Digraph<String>()

        MicroModule microModule = Utils.buildMicroModule(project, ':main')
        if (microModule != null) {
            setMainMicroModule(microModule)
        }
    }

    void setMainMicroModule(MicroModule microModule) {
        if (microModule == null) {
            throw new GradleException("main MicroModule cannot be null.")
        }
        this.mainMicroModule = microModule
        addMicroModule(microModule)
    }

    void addMicroModule(MicroModule microModule) {
        for (int i = 0; i < includeMicroModules.size(); i++) {
            if (includeMicroModules.get(i).name.equals(microModule.name)) {
                return
            }
        }
        includeMicroModules.add(microModule)
    }

    MicroModule getMicroModule(String name) {
        for (int i = 0; i < includeMicroModules.size(); i++) {
            MicroModule microModule = includeMicroModules.get(i)
            if (includeMicroModules.get(i).name.equals(name)) {
                return microModule
            }
        }
        return null
    }

    List<String> getMicroModuleDependency(String name) {
        return microModuleDependency.get(name)
    }

    void setMicroModuleDependency(String target, String dependency) {
        MicroModule dependencyMicroModule = getMicroModule(dependency)
        if(dependencyMicroModule == null) {
            if(Utils.buildMicroModule(project, dependency) != null) {
                throw new GradleException("MicroModule '${microModule.name}' dependency MicroModle '${name}', but its not included.")
            } else {
                throw new GradleException("MicroModule with path '${path}' could not be found in ${project.getDisplayName()}.")
            }
        }

        dependencyGraph.add(target, dependency)
        if(!dependencyGraph.isDag()) {
            throw new GradleException("Circular dependency between MicroModule '${target}' and '${dependency}'.")
        }
        List<String> dependencyList = microModuleDependency.get(target)
        if (dependencyList == null) {
            dependencyList = new ArrayList<>()
            dependencyList.add(dependency)
            microModuleDependency.put(target, dependencyList)
        } else {
            if (!dependencyList.contains(dependency)) {
                dependencyList.add(dependency)
            }
        }
    }

    boolean isMicroModuleIncluded(String name) {
        boolean included = false
        includeMicroModules.each {
            if (it.name == name) {
                included = true
            }
        }
        return included
    }

}