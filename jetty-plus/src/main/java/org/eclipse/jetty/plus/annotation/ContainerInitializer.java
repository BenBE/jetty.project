//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.plus.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContainerInitializer;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class ContainerInitializer
{
    private static final Logger LOG = Log.getLogger(ContainerInitializer.class);
    
    final protected ServletContainerInitializer _target;
    final protected Class<?>[] _interestedTypes;
    final protected Set<String> _applicableTypeNames = new ConcurrentHashSet<String>();
    final protected Set<String> _annotatedTypeNames = new ConcurrentHashSet<String>();


    public ContainerInitializer (ServletContainerInitializer target, Class<?>[] classes)
    {
        _target = target;
        _interestedTypes = classes;
    }
    
    public ContainerInitializer (ClassLoader loader, String toString)
    {
    	Matcher m = Pattern.compile("ContainerInitializer\\{(.*),interested=(.*),applicable=(.*),annotated=(.*)\\}").matcher(toString);
    	if (!m.matches())
    		throw new IllegalArgumentException(toString);
    	
    	try
    	{
    		_target = (ServletContainerInitializer)loader.loadClass(m.group(1)).newInstance();
    		String[] interested = StringUtil.arrayFromString(m.group(2));
    		_interestedTypes = new Class<?>[interested.length];
    		for (int i=0;i<interested.length;i++)
    			_interestedTypes[i]=loader.loadClass(interested[i]);
    		for (String s:StringUtil.arrayFromString(m.group(3)))
    			_applicableTypeNames.add(s);
    		for (String s:StringUtil.arrayFromString(m.group(4)))
    			_annotatedTypeNames.add(s);
    	}
    	catch(Exception e)
    	{
    		throw new IllegalArgumentException(toString, e);
    	}
    }
    
    public ServletContainerInitializer getTarget ()
    {
        return _target;
    }

    public Class[] getInterestedTypes ()
    {
        return _interestedTypes;
    }


    /**
     * A class has been found that has an annotation of interest
     * to this initializer.
     * @param className
     */
    public void addAnnotatedTypeName (String className)
    {
        _annotatedTypeNames.add(className);
    }

    public Set<String> getAnnotatedTypeNames ()
    {
        return Collections.unmodifiableSet(_annotatedTypeNames);
    }

    public void addApplicableTypeName (String className)
    {
        _applicableTypeNames.add(className);
    }

    public Set<String> getApplicableTypeNames ()
    {
        return Collections.unmodifiableSet(_applicableTypeNames);
    }


    public void callStartup(WebAppContext context)
    throws Exception
    {
        if (_target != null)
        {
            Set<Class<?>> classes = new HashSet<Class<?>>();

            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());

            try
            {
                for (String s : _applicableTypeNames)
                    classes.add(Loader.loadClass(context.getClass(), s));

                context.getServletContext().setExtendedListenerTypes(true);
                if (LOG.isDebugEnabled())
                {
                    long start = System.nanoTime();
                    _target.onStartup(classes, context.getServletContext());
                    LOG.debug("ContainerInitializer {} called in {}ms", _target.getClass().getName(), TimeUnit.MILLISECONDS.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS));
                }
                else
                    _target.onStartup(classes, context.getServletContext());
            }
            finally
            { 
                context.getServletContext().setExtendedListenerTypes(false);
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
    }
    
    public String toString()
    {
    	List<String> interested = new ArrayList<>(_interestedTypes.length);
    	for (Class<?> c : _interestedTypes)
    		interested.add(c.getName());
    	return String.format("ContainerInitializer{%s,interested=%s,applicable=%s,annotated=%s}",_target.getClass().getName(),interested,_applicableTypeNames,_annotatedTypeNames);
    }
}
