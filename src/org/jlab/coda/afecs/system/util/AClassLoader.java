/*
 *   Copyright (c) 2017.  Jefferson Lab (JLab). All rights reserved. Permission
 *   to use, copy, modify, and distribute  this software and its documentation for
 *   governmental use, educational, research, and not-for-profit purposes, without
 *   fee and without a signed licensing agreement.
 *
 *   IN NO EVENT SHALL JLAB BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL
 *   INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
 *   OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF JLAB HAS
 *   BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   JLAB SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE. THE CLARA SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 *   PROVIDED HEREUNDER IS PROVIDED "AS IS". JLAB HAS NO OBLIGATION TO PROVIDE
 *   MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *   This software was developed under the United States Government license.
 *   For more information contact author at gurjyan@jlab.org
 *   Department of Experimental Nuclear Physics, Jefferson Lab.
 */

package org.jlab.coda.afecs.system.util;

import java.net.URL;
import java.net.URLClassLoader;


/**
 * <p>
 *     Class loader
 * </p>
 *
 * @author gurjyan
 *         Date: 11/13/14 Time: 2:51 PM
 * @version 4.x
 */
public class AClassLoader extends URLClassLoader {

    /** Name of the classes we want loaded. */
    String[] classesToLoad;

    /**
     * Constructor. The system class loader is assumed to be the parent.
     *
     * @param urls URLs to search for the classes of interest.
     */
    public AClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * Set the name of the classes we want to load.
     *
     * @param classNames names of the classes we want to load
     */
    public void setClassesToLoad(String[] classNames) {
        classesToLoad = classNames;
    }

    /**
     * Method to load the class of interest. This is called many
     * times by the system - once for each class that the class
     * we are trying to load references.
     *
     * @param name name of class to load
     * @return Class object
     * @throws ClassNotFoundException if code for class cannot be found
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        // Default to standard URLClassLoader behavior
        if (classesToLoad == null) return getParent().loadClass(name);

        boolean matches = false;
        for (String clazz : classesToLoad) {
            if (name.contains(clazz)) {
                matches = true;
                break;
            }
        }

        if (!matches) {
            return getParent().loadClass(name);
        }

        // If this loader has already loaded the class, return it since it
        // is not allowed to load things twice.
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        c = findClass(name);
        return c;
    }

}

