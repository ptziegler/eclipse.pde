package org.eclipse.pde.internal.core;

import java.io.*;

import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.runtime.model.*;

public class PDERegistryCacheReader {

	Factory cacheFactory;
	// objectTable will be an array list of objects.  The objects will be things
	// like a plugin descriptor, extension, extension point, etc.  The integer
	// index value will be used in the cache to allow cross-references in the
	// cached registry.
	ArrayList objectTable = null;

	public static final byte REGISTRY_CACHE_VERSION = 9;

	public static final byte NONLABEL = 0;

	public static final byte CONFIGURATION_ELEMENT_END_LABEL = 1;
	public static final byte CONFIGURATION_ELEMENT_INDEX_LABEL = 45;
	public static final byte CONFIGURATION_ELEMENT_LABEL = 2;
	public static final byte CONFIGURATION_ELEMENT_PARENT_LABEL = 3;
	public static final byte CONFIGURATION_PROPERTY_END_LABEL = 4;
	public static final byte CONFIGURATION_PROPERTY_LABEL = 5;

	public static final byte EXTENSION_END_LABEL = 6;
	public static final byte EXTENSION_EXT_POINT_NAME_LABEL = 7;
	public static final byte EXTENSION_INDEX_LABEL = 8;
	public static final byte EXTENSION_PARENT_LABEL = 9;

	public static final byte EXTENSION_POINT_END_LABEL = 10;
	public static final byte EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL = 11;
	public static final byte EXTENSION_POINT_EXTENSIONS_LABEL = 12;
	public static final byte EXTENSION_POINT_PARENT_LABEL = 13;
	public static final byte EXTENSION_POINT_SCHEMA_LABEL = 14;
	public static final byte EXTENSION_POINT_INDEX_LABEL = 56;

	public static final byte FRAGMENT_INDEX_LABEL = 47;
	public static final byte FRAGMENT_LABEL = 48;
	public static final byte FRAGMENT_END_LABEL = 49;
	public static final byte FRAGMENT_PLUGIN_LABEL = 50;
	public static final byte FRAGMENT_PLUGIN_VERSION_LABEL = 51;
	public static final byte FRAGMENT_PLUGIN_MATCH_LABEL = 55;

	public static final byte ID_LABEL = 15;
	public static final byte LIBRARY_END_LABEL = 16;
	public static final byte LIBRARY_EXPORTS_LABEL = 17;
	public static final byte LIBRARY_EXPORTS_LENGTH_LABEL = 18;
	public static final byte NAME_LABEL = 19;
	public static final byte LIBRARY_INDEX_LABEL = 57;
	public static final byte START_LINE = 59;

	public static final byte PLUGIN_CLASS_LABEL = 20;
	public static final byte PLUGIN_ENABLED_LABEL = 21;
	public static final byte PLUGIN_END_LABEL = 22;
	public static final byte PLUGIN_EXTENSION_LABEL = 23;
	public static final byte PLUGIN_EXTENSION_POINT_LABEL = 24;
	public static final byte PLUGIN_INDEX_LABEL = 25;
	public static final byte PLUGIN_LABEL = 26;
	public static final byte PLUGIN_LOCATION_LABEL = 27;
	public static final byte PLUGIN_LIBRARY_LABEL = 28;
	public static final byte PLUGIN_PARENT_LABEL = 29;
	public static final byte PLUGIN_PROVIDER_NAME_LABEL = 30;
	public static final byte PLUGIN_REQUIRES_LABEL = 31;

	public static final byte PROPERTIES_LENGTH_LABEL = 32;
	public static final byte READONLY_LABEL = 33;
	public static final byte REGISTRY_END_LABEL = 34;
	public static final byte REGISTRY_INDEX_LABEL = 46;
	public static final byte REGISTRY_LABEL = 35;
	public static final byte REGISTRY_RESOLVED_LABEL = 36;
	public static final byte REQUIRES_END_LABEL = 37;
	public static final byte REQUIRES_EXPORT_LABEL = 38;
	public static final byte REQUIRES_MATCH_LABEL = 39;
	public static final byte REQUIRES_OPTIONAL_LABEL = 52;
	public static final byte REQUIRES_PLUGIN_NAME_LABEL = 40;
	public static final byte REQUIRES_RESOLVED_VERSION_LABEL = 41;
	public static final byte REQUIRES_INDEX_LABEL = 58;
	public static final byte SOURCE_LABEL = 53;
	public static final byte SUBELEMENTS_LENGTH_LABEL = 42;
	public static final byte TYPE_LABEL = 54;
	public static final byte VALUE_LABEL = 43;
	public static final byte VERSION_LABEL = 44;

	// So it's easier to add a new label ...
	public static final byte LARGEST_LABEL = 59;

	// String constants for those byte values in the cache that
	// do not translate directly to strings found in manifest xml
	// files.  For example, the 'location' of a plugin or fragment
	// is not listed in a plugin.xml file.  The parser fills in
	// this field after the plugin.xml file has been successfully
	// parsed and added to the plugin registry.  Therefore, 'location'
	// will not appear in plugin.xml (and not in ICoreModel either) but
	// will appear in the registry cache.
	private static final String RESOLVED = "resolved"; //$NON-NLS-1$
	private static final String READONLY = "readonly"; //$NON-NLS-1$
	private static final String LOCATION = "location"; //$NON-NLS-1$
	private static final String ENABLED = "enabled"; //$NON-NLS-1$
	private static final String RESOLVED_VERSION = "resolved_version"; //$NON-NLS-1$
	private static final String END = "end"; //$NON-NLS-1$
	private static final String EXPORTS_LENGTH = "<length of export list>"; //$NON-NLS-1$
	private static final String SUBELEMENTS_LENGTH = "<length of subelement list>"; //$NON-NLS-1$
	private static final String PROPERTIES_LENGTH = "<length of properties list>"; //$NON-NLS-1$
	private static final String PARENT_REGISTRY = "<index of parent registry>"; //$NON-NLS-1$
	private static final String CONFIGURATION_ELEMENT_PARENT = "<index of element parent>"; //$NON-NLS-1$
	private static final String PLUGIN_INDEX = "<index of plugin>"; //$NON-NLS-1$
	private static final String EXTENSION_INDEX = "<index of extension>"; //$NON-NLS-1$
	private static final String EXT_PT_PARENT_INDEX = "<index of extension point parent>"; //$NON-NLS-1$
	private static final String EXT_PT_EXTENSION_LENGTH = "<length of extension list>"; //$NON-NLS-1$
	private static final String EXT_LIST = "<list of extensions>"; //$NON-NLS-1$
	private static final String EXTENSION_PARENT = "<index of extension parent>"; //$NON-NLS-1$
	private static final String ELEMENT_INDEX = "<index of element>"; //$NON-NLS-1$
	private static final String REGISTRY_INDEX = "<index of registry>"; //$NON-NLS-1$
	private static final String FRAGMENT_INDEX = "<index of fragment>"; //$NON-NLS-1$
	private static final String EXTENSION_POINT_INDEX = "<index of extension point>"; //$NON-NLS-1$
	private static final String LIBRARY_INDEX = "<index of library>"; //$NON-NLS-1$
	private static final String REQUIRES_INDEX = "<index of prerequisite>"; //$NON-NLS-1$
	private static final String UNKNOWN = "<unknown label>"; //$NON-NLS-1$

	private long code;

	public PDERegistryCacheReader(Factory factory, long code) {
		super();
		cacheFactory = factory;
		objectTable = null;
		this.code = code;
	}
	private int addToObjectTable(Object object) {
		if (objectTable == null) {
			objectTable = new ArrayList();
		}
		objectTable.add(object);
		// return the index of the object just added (i.e. size - 1)
		return (objectTable.size() - 1);
	}
	private void debug(String msg) {
		System.out.println("RegistryCacheReader: " + msg); //$NON-NLS-1$
	}
	public static String decipherLabel(byte labelValue) {
		// Change a byte value from the registry cache into a
		// human readable string.
		String retValue = "\""; //$NON-NLS-1$
		switch (labelValue) {
			case REGISTRY_LABEL :
				retValue += ICoreModel.REGISTRY;
				break;
			case REGISTRY_RESOLVED_LABEL :
				retValue += RESOLVED;
				break;
			case PLUGIN_LABEL :
				retValue += ICoreModel.PLUGIN;
				break;
			case REGISTRY_END_LABEL :
				retValue += END + ICoreModel.REGISTRY;
				break;
			case READONLY_LABEL :
				retValue += READONLY;
				break;
			case NAME_LABEL :
				retValue += ICoreModel.PLUGIN_NAME;
				break;
			case ID_LABEL :
				retValue += ICoreModel.PLUGIN_ID;
				break;
			case PLUGIN_PROVIDER_NAME_LABEL :
				retValue += ICoreModel.PLUGIN_PROVIDER;
				break;
			case VERSION_LABEL :
				retValue += ICoreModel.PLUGIN_VERSION;
				break;
			case PLUGIN_CLASS_LABEL :
				retValue += ICoreModel.PLUGIN_CLASS;
				break;
			case PLUGIN_LOCATION_LABEL :
				retValue += LOCATION;
				break;
			case PLUGIN_ENABLED_LABEL :
				retValue += ENABLED;
				break;
			case PLUGIN_REQUIRES_LABEL :
				retValue += ICoreModel.PLUGIN_REQUIRES;
				break;
			case PLUGIN_LIBRARY_LABEL :
				retValue += ICoreModel.LIBRARY;
				break;
			case PLUGIN_EXTENSION_LABEL :
				retValue += ICoreModel.EXTENSION;
				break;
			case PLUGIN_EXTENSION_POINT_LABEL :
				retValue += ICoreModel.EXTENSION_POINT;
				break;
			case PLUGIN_END_LABEL :
				retValue += END + ICoreModel.PLUGIN;
				break;
			case REQUIRES_MATCH_LABEL :
				retValue += ICoreModel.PLUGIN_REQUIRES_MATCH;
				break;
			case REQUIRES_EXPORT_LABEL :
				retValue += ICoreModel.PLUGIN_REQUIRES_EXPORT;
				break;
			case REQUIRES_RESOLVED_VERSION_LABEL :
				retValue += RESOLVED_VERSION;
				break;
			case REQUIRES_PLUGIN_NAME_LABEL :
				retValue += ICoreModel.PLUGIN_REQUIRES_PLUGIN;
				break;
			case REQUIRES_END_LABEL :
				retValue += END + ICoreModel.PLUGIN_REQUIRES;
				break;
			case LIBRARY_EXPORTS_LENGTH_LABEL :
				retValue += EXPORTS_LENGTH;
				break;
			case LIBRARY_EXPORTS_LABEL :
				retValue += ICoreModel.LIBRARY_EXPORT;
				break;
			case LIBRARY_END_LABEL :
				retValue += END + ICoreModel.LIBRARY;
				break;
			case EXTENSION_POINT_SCHEMA_LABEL :
				retValue += ICoreModel.EXTENSION_POINT_SCHEMA;
				break;
			case EXTENSION_POINT_END_LABEL :
				retValue += END + ICoreModel.EXTENSION_POINT;
				break;
			case EXTENSION_EXT_POINT_NAME_LABEL :
				retValue += ICoreModel.EXTENSION_TARGET;
				break;
			case SUBELEMENTS_LENGTH_LABEL :
				retValue += SUBELEMENTS_LENGTH;
				break;
			case EXTENSION_END_LABEL :
				retValue += END + ICoreModel.EXTENSION;
				break;
			case CONFIGURATION_ELEMENT_LABEL :
				retValue += ICoreModel.ELEMENT;
				break;
			case VALUE_LABEL :
				retValue += ICoreModel.ELEMENT_VALUE;
				break;
			case PROPERTIES_LENGTH_LABEL :
				retValue += PROPERTIES_LENGTH;
				break;
			case CONFIGURATION_ELEMENT_END_LABEL :
				retValue += END + ICoreModel.ELEMENT;
				break;
			case CONFIGURATION_PROPERTY_LABEL :
				retValue += ICoreModel.PROPERTY;
				break;
			case CONFIGURATION_PROPERTY_END_LABEL :
				retValue += END + ICoreModel.PROPERTY;
				break;
			case PLUGIN_PARENT_LABEL :
				retValue += PARENT_REGISTRY;
				break;
			case CONFIGURATION_ELEMENT_PARENT_LABEL :
				retValue += CONFIGURATION_ELEMENT_PARENT;
				break;
			case PLUGIN_INDEX_LABEL :
				retValue += PLUGIN_INDEX;
				break;
			case EXTENSION_INDEX_LABEL :
				retValue += EXTENSION_INDEX;
				break;
			case EXTENSION_POINT_PARENT_LABEL :
				retValue += EXT_PT_PARENT_INDEX;
				break;
			case EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL :
				retValue += EXT_PT_EXTENSION_LENGTH;
				break;
			case EXTENSION_POINT_EXTENSIONS_LABEL :
				retValue += EXT_LIST;
				break;
			case EXTENSION_PARENT_LABEL :
				retValue += EXTENSION_PARENT;
				break;
			case CONFIGURATION_ELEMENT_INDEX_LABEL :
				retValue += ELEMENT_INDEX;
				break;
			case REGISTRY_INDEX_LABEL :
				retValue += REGISTRY_INDEX;
				break;
			case FRAGMENT_END_LABEL :
				retValue += END + ICoreModel.FRAGMENT;
				break;
			case FRAGMENT_INDEX_LABEL :
				retValue += FRAGMENT_INDEX;
				break;
			case FRAGMENT_LABEL :
				retValue += ICoreModel.FRAGMENT;
				break;
			case FRAGMENT_PLUGIN_LABEL :
				retValue += ICoreModel.FRAGMENT_PLUGIN_ID;
				break;
			case FRAGMENT_PLUGIN_MATCH_LABEL :
				retValue += ICoreModel.FRAGMENT_PLUGIN_MATCH;
				break;
			case FRAGMENT_PLUGIN_VERSION_LABEL :
				retValue += ICoreModel.FRAGMENT_PLUGIN_VERSION;
				break;
			case REQUIRES_OPTIONAL_LABEL :
				retValue += ICoreModel.PLUGIN_REQUIRES_OPTIONAL;
				break;
			case SOURCE_LABEL :
				retValue += ICoreModel.LIBRARY_SOURCE;
				break;
			case TYPE_LABEL :
				retValue += ICoreModel.LIBRARY_TYPE;
				break;
			case EXTENSION_POINT_INDEX_LABEL :
				retValue += EXTENSION_POINT_INDEX;
				break;
			case LIBRARY_INDEX_LABEL :
				retValue += LIBRARY_INDEX;
				break;
			case REQUIRES_INDEX_LABEL :
				retValue += REQUIRES_INDEX;
				break;
			default :
				retValue += UNKNOWN;
				break;
		}

		retValue += "\""; //$NON-NLS-1$
		return retValue;
	}
	public boolean interpretHeaderInformation(DataInputStream in) {
		try {
			return (in.readLong() == code);
		} catch (IOException e) {
			return false;
		}
	}
	public ConfigurationElementModel readConfigurationElement(
		DataInputStream in,
		boolean debugFlag) {
		ConfigurationElementModel configurationElement =
			cacheFactory.createConfigurationElement();
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// configuration element.
		addToObjectTable(configurationElement);
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						configurationElement.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						configurationElement.setName(in.readUTF());
						break;
					case VALUE_LABEL :
						configurationElement.setValue(in.readUTF());
						break;
					case PROPERTIES_LENGTH_LABEL :
						int propertiesLength = in.readInt();
						ConfigurationPropertyModel[] properties =
							new ConfigurationPropertyModel[propertiesLength];
						for (int i = 0; i < propertiesLength && !done; i++) {
							properties[i] =
								readConfigurationProperty(in, debugFlag);
							if (properties[i] == null) {
								// Something went wrong reading this configuration
								// property
								if (debugFlag) {
									String name =
										configurationElement.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug("Trouble reading configuration property #" + i + " for configuration element " + name); //$NON-NLS-1$ //$NON-NLS-2$
								}
								configurationElement = null;
								done = true;
							}
						}
						if (configurationElement != null)
							configurationElement.setProperties(properties);
						properties = null;
						break;
					case SUBELEMENTS_LENGTH_LABEL :
						int subElementsLength = in.readInt();
						ConfigurationElementModel[] subElements =
							new ConfigurationElementModel[subElementsLength];
						for (int i = 0; i < subElementsLength && !done; i++) {
							// Do we have an index or a real configuration element?
							byte subInByte = in.readByte();
							switch (subInByte) {
								case CONFIGURATION_ELEMENT_LABEL :
									subElements[i] =
										readConfigurationElement(in, debugFlag);
									if (subElements[i] == null) {
										if (debugFlag) {
											String name =
												configurationElement.getName();
											if (name == null)
												name = new String("<unknown name>"); //$NON-NLS-1$
											debug("Unable to read subelement #" + i + " for configuration element " + name); //$NON-NLS-1$ //$NON-NLS-2$
										}
										configurationElement = null;
										done = true;
									}
									break;
								case CONFIGURATION_ELEMENT_INDEX_LABEL :
									subElements[i] =
										(
											ConfigurationElementModel) objectTable
												.get(
											in.readInt());
									break;
								default :
									// We found something we weren't expecting
									if (debugFlag) {
										String name =
											configurationElement.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug("Unexpected byte code " + decipherLabel(subInByte) + "reading subelements of configuration element" + name); //$NON-NLS-1$ //$NON-NLS-2$
									}
									done = true;
									configurationElement = null;
									break;
							}
						}
						if (configurationElement != null)
							configurationElement.setSubElements(subElements);
						subElements = null;
						break;
					case CONFIGURATION_ELEMENT_PARENT_LABEL :
						// We know the parent already exists, just grab it.
						configurationElement.setParent(
							objectTable.get(in.readInt()));
						break;
					case CONFIGURATION_ELEMENT_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = configurationElement.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + "reading configuration element" + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						done = true;
						configurationElement = null;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return configurationElement;
	}
	public ConfigurationPropertyModel readConfigurationProperty(
		DataInputStream in,
		boolean debugFlag) {
		ConfigurationPropertyModel configurationProperty =
			cacheFactory.createConfigurationProperty();
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// configuration property.
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case CONFIGURATION_PROPERTY_LABEL :
						break;
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						configurationProperty.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						configurationProperty.setName(in.readUTF());
						break;
					case VALUE_LABEL :
						configurationProperty.setValue(in.readUTF());
						break;
					case CONFIGURATION_PROPERTY_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = configurationProperty.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading configuration property " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						configurationProperty = null;
						done = true;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return configurationProperty;
	}
	public ExtensionModel readExtension(
		DataInputStream in,
		boolean debugFlag) {
		ExtensionModel extension = cacheFactory.createExtension();
		addToObjectTable(extension);
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// extension.
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						extension.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						extension.setName(in.readUTF());
						break;
					case ID_LABEL :
						extension.setId(in.readUTF());
						break;
					case EXTENSION_EXT_POINT_NAME_LABEL :
						extension.setExtensionPoint(in.readUTF());
						break;
					case SUBELEMENTS_LENGTH_LABEL :
						int subElementsLength = in.readInt();
						ConfigurationElementModel[] subElements =
							new ConfigurationElementModel[subElementsLength];
						for (int i = 0; i < subElementsLength && !done; i++) {
							// Do we have a configuration element or an index into
							// objectTable?
							byte subInByte = in.readByte();
							switch (subInByte) {
								case CONFIGURATION_ELEMENT_LABEL :
									subElements[i] =
										readConfigurationElement(in, debugFlag);
									if (subElements[i] == null) {
										if (debugFlag) {
											String name = extension.getName();
											if (name == null)
												name = new String("<unknown name>"); //$NON-NLS-1$
											debug("Unable to read subelement #" + i + " for extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
										}
										extension = null;
										done = true;
									}
									break;
								case CONFIGURATION_ELEMENT_INDEX_LABEL :
									subElements[i] =
										(
											ConfigurationElementModel) objectTable
												.get(
											in.readInt());
									break;
								default :
									// We got something unexpected
									if (debugFlag) {
										String name = extension.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug("Unexpected byte code " + decipherLabel(subInByte) + " reading subelements for extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
									}
									extension = null;
									done = true;
									break;
							}
						}
						if (extension != null)
							extension.setSubElements(subElements);
						subElements = null;
						break;
					case EXTENSION_PARENT_LABEL :
						// Either there is a plugin or there is an index into the
						// objectTable
						byte subByte = in.readByte();
						switch (subByte) {
							case PLUGIN_LABEL :
								PluginModel parent =
									(PluginModel) readPluginDescriptor(in,
										debugFlag);
								if (parent == null) {
									if (debugFlag) {
										String name = extension.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug("Trouble reading parent plugin for extension " + name); //$NON-NLS-1$
									}
									done = true;
									extension = null;
								} else {
									extension.setParent(parent);
								}
								break;
							case PLUGIN_INDEX_LABEL :
								extension.setParent(
									(PluginModel) objectTable.get(
										in.readInt()));
								break;
							case FRAGMENT_LABEL :
								PluginModel fragmentParent =
									(PluginModel) readPluginFragment(in,
										debugFlag);
								if (fragmentParent == null) {
									if (debugFlag) {
										String name = extension.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug("Trouble reading parent fragment for extension " + name); //$NON-NLS-1$
									}
									done = true;
									extension = null;
								} else {
									extension.setParent(fragmentParent);
								}
								break;
							case FRAGMENT_INDEX_LABEL :
								extension.setParent(
									(PluginModel) objectTable.get(
										in.readInt()));
								break;
							default :
								// We got something unexpected
								if (debugFlag) {
									String name = extension.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug("Unexpected byte code " + decipherLabel(subByte) + "reading parent of extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
								}
								done = true;
								extension = null;
								break;
						}
						break;
					case EXTENSION_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = extension.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + "reading extension" + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						done = true;
						extension = null;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return extension;
	}
	public ExtensionPointModel readExtensionPoint(
		DataInputStream in,
		boolean debugFlag) {
		ExtensionPointModel extPoint = cacheFactory.createExtensionPoint();
		addToObjectTable(extPoint);

		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// extension point.
		int extensionLength = 0;
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						extPoint.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						extPoint.setName(in.readUTF());
						break;
					case ID_LABEL :
						extPoint.setId(in.readUTF());
						break;
					case EXTENSION_POINT_SCHEMA_LABEL :
						extPoint.setSchema(in.readUTF());
						break;
					case EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL :
						extensionLength = in.readInt();
						break;
					case EXTENSION_POINT_EXTENSIONS_LABEL :
						ExtensionModel[] extensions =
							new ExtensionModel[extensionLength];
						for (int i = 0; i < extensionLength && !done; i++) {
							byte subByte = in.readByte();
							switch (subByte) {
								// Either this is an extension or an index into
								// the objectTable
								case PLUGIN_EXTENSION_LABEL :
									extensions[i] =
										readExtension(in, debugFlag);
									if (extensions[i] == null) {
										if (debugFlag) {
											String name = extPoint.getName();
											if (name == null)
												name = new String("<unknown name>"); //$NON-NLS-1$
											debug("Unable to read extension #" + i + " for extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$
										}
										done = true;
										extPoint = null;
									}
									break;
								case EXTENSION_INDEX_LABEL :
									extensions[i] =
										(ExtensionModel) objectTable.get(
											in.readInt());
									break;
								default :
									// We got something unexpected
									if (debugFlag) {
										String name = extPoint.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug("Unexpected byte code " + decipherLabel(subByte) + "reading extension #" + i + " for extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									}
									extPoint = null;
									done = true;
									break;
							}
						}
						if (extPoint != null)
							extPoint.setDeclaredExtensions(extensions);
						break;
					case EXTENSION_POINT_PARENT_LABEL :
						// We know this plugin or fragment is already in the objectTable
						extPoint.setParent(
							(PluginModel) objectTable.get(in.readInt()));
						break;
					case EXTENSION_POINT_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = extPoint.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						extPoint = null;
						done = true;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return extPoint;
	}
	public LibraryModel readLibrary(DataInputStream in, boolean debugFlag) {
		LibraryModel library = cacheFactory.createLibrary();
		addToObjectTable(library);
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// library.
		int exportsLength = 0;
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						library.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						library.setName(in.readUTF());
						break;
					case LIBRARY_EXPORTS_LENGTH_LABEL :
						exportsLength = in.readInt();
						break;
					case TYPE_LABEL :
						library.setType(in.readUTF());
						break;
					case LIBRARY_EXPORTS_LABEL :
						String[] exports = new String[exportsLength];
						for (int i = 0; i < exportsLength && !done; i++) {
							exports[i] = in.readUTF();
							if (exports[i] == null) {
								if (debugFlag) {
									String name = library.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug("Empty export string for export #" + i + " reading library " + name); //$NON-NLS-1$ //$NON-NLS-2$
								}
								done = true;
								library = null;
							}
						}
						if (!done)
							library.setExports(exports);
						exports = null;
						break;
					case LIBRARY_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = library.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading library " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						library = null;
						done = true;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return library;
	}
	public PluginDescriptorModel readPluginDescriptor(
		DataInputStream in,
		boolean debugFlag) {
		PluginDescriptorModel plugin = cacheFactory.createPluginDescriptor();
		addToObjectTable(plugin);
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// plugin.
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						plugin.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						plugin.setName(in.readUTF());
						break;
					case ID_LABEL :
						plugin.setId(in.readUTF());
						break;
					case PLUGIN_PROVIDER_NAME_LABEL :
						plugin.setProviderName(in.readUTF());
						break;
					case VERSION_LABEL :
						plugin.setVersion(in.readUTF());
						break;
					case PLUGIN_CLASS_LABEL :
						plugin.setPluginClass(in.readUTF());
						break;
					case PLUGIN_LOCATION_LABEL :
						plugin.setLocation(in.readUTF());
						break;
					case PLUGIN_ENABLED_LABEL :
						plugin.setEnabled(in.readBoolean());
						break;
					case PLUGIN_REQUIRES_LABEL :
						PluginPrerequisiteModel requires =
							readPluginPrerequisite(in, debugFlag);
						if (requires == null) {
							// Something went wrong
							if (debugFlag) {
								String name = plugin.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read prerequisite for plugin " + name); //$NON-NLS-1$
							}
							plugin = null;
							done = true;
						} else {
							// Add this prerequisite to the end of the requires list
							PluginPrerequisiteModel[] requiresList =
								plugin.getRequires();
							PluginPrerequisiteModel[] newRequiresValues = null;
							if (requiresList == null) {
								newRequiresValues =
									new PluginPrerequisiteModel[1];
								newRequiresValues[0] = requires;
							} else {
								newRequiresValues =
									new PluginPrerequisiteModel[requiresList
										.length
										+ 1];
								System.arraycopy(
									requiresList,
									0,
									newRequiresValues,
									0,
									requiresList.length);
								newRequiresValues[requiresList.length] =
									requires;
							}
							plugin.setRequires(newRequiresValues);
							requiresList = newRequiresValues = null;
						}
						requires = null;
						break;
					case REQUIRES_INDEX_LABEL :
						requires =
							(PluginPrerequisiteModel) objectTable.get(
								in.readInt());
						PluginPrerequisiteModel[] requiresList =
							plugin.getRequires();
						PluginPrerequisiteModel[] newRequiresValues = null;
						if (requiresList == null) {
							newRequiresValues = new PluginPrerequisiteModel[1];
							newRequiresValues[0] = requires;
						} else {
							newRequiresValues =
								new PluginPrerequisiteModel[requiresList.length
									+ 1];
							System.arraycopy(
								requiresList,
								0,
								newRequiresValues,
								0,
								requiresList.length);
							newRequiresValues[requiresList.length] = requires;
						}
						plugin.setRequires(newRequiresValues);
						requires = null;
						requiresList = newRequiresValues = null;
						break;
					case PLUGIN_LIBRARY_LABEL :
						LibraryModel library = readLibrary(in, debugFlag);
						if (library == null) {
							// Something went wrong reading this library
							if (debugFlag) {
								String name = plugin.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read library for plugin " + name); //$NON-NLS-1$
							}
							plugin = null;
							done = true;
						} else {
							// Add this library to the end of the runtime list
							LibraryModel[] libraryList = plugin.getRuntime();
							LibraryModel[] newLibraryValues = null;
							if (libraryList == null) {
								newLibraryValues = new LibraryModel[1];
								newLibraryValues[0] = library;
							} else {
								newLibraryValues =
									new LibraryModel[libraryList.length + 1];
								System.arraycopy(
									libraryList,
									0,
									newLibraryValues,
									0,
									libraryList.length);
								newLibraryValues[libraryList.length] = library;
							}
							plugin.setRuntime(newLibraryValues);
							libraryList = newLibraryValues = null;
						}
						library = null;
						break;
					case LIBRARY_INDEX_LABEL :
						library = (LibraryModel) objectTable.get(in.readInt());
						LibraryModel[] libraryList = plugin.getRuntime();
						LibraryModel[] newLibraryValues = null;
						if (libraryList == null) {
							newLibraryValues = new LibraryModel[1];
							newLibraryValues[0] = library;
						} else {
							newLibraryValues =
								new LibraryModel[libraryList.length + 1];
							System.arraycopy(
								libraryList,
								0,
								newLibraryValues,
								0,
								libraryList.length);
							newLibraryValues[libraryList.length] = library;
						}
						plugin.setRuntime(newLibraryValues);
						library = null;
						libraryList = newLibraryValues = null;
						break;
					case PLUGIN_EXTENSION_LABEL :
						ExtensionModel extension = readExtension(in, debugFlag);
						if (extension == null) {
							// Something went wrong reading this extension
							if (debugFlag) {
								String name = plugin.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read extension for plugin " + name); //$NON-NLS-1$
							}
							plugin = null;
							done = true;
						} else {
							ExtensionModel[] extList =
								plugin.getDeclaredExtensions();
							ExtensionModel[] newExtValues = null;
							if (extList == null) {
								newExtValues = new ExtensionModel[1];
								newExtValues[0] = extension;
							} else {
								newExtValues =
									new ExtensionModel[extList.length + 1];
								System.arraycopy(
									extList,
									0,
									newExtValues,
									0,
									extList.length);
								newExtValues[extList.length] = extension;
							}
							plugin.setDeclaredExtensions(newExtValues);
							extList = newExtValues = null;
						}
						extension = null;
						break;
					case EXTENSION_INDEX_LABEL :
						extension =
							(ExtensionModel) objectTable.get(in.readInt());
						ExtensionModel[] extList =
							plugin.getDeclaredExtensions();
						ExtensionModel[] newExtValues = null;
						if (extList == null) {
							newExtValues = new ExtensionModel[1];
							newExtValues[0] = extension;
						} else {
							newExtValues =
								new ExtensionModel[extList.length + 1];
							System.arraycopy(
								extList,
								0,
								newExtValues,
								0,
								extList.length);
							newExtValues[extList.length] = extension;
						}
						plugin.setDeclaredExtensions(newExtValues);
						extension = null;
						extList = newExtValues = null;
						break;
					case PLUGIN_EXTENSION_POINT_LABEL :
						ExtensionPointModel extensionPoint =
							readExtensionPoint(in, debugFlag);
						if (extensionPoint == null) {
							// Something went wrong reading this extension
							if (debugFlag) {
								String name = plugin.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read extension point for plugin " + name); //$NON-NLS-1$
							}
							plugin = null;
							done = true;
						} else {
							// Add this extension point to the end of the extension point list
							ExtensionPointModel[] extPointList =
								plugin.getDeclaredExtensionPoints();
							ExtensionPointModel[] newExtPointValues = null;
							if (extPointList == null) {
								newExtPointValues = new ExtensionPointModel[1];
								newExtPointValues[0] = extensionPoint;
							} else {
								newExtPointValues =
									new ExtensionPointModel[extPointList.length
										+ 1];
								System.arraycopy(
									extPointList,
									0,
									newExtPointValues,
									0,
									extPointList.length);
								newExtPointValues[extPointList.length] =
									extensionPoint;
							}
							plugin.setDeclaredExtensionPoints(
								newExtPointValues);
							extensionPoint = null;
							extPointList = newExtPointValues = null;
						}
						break;
					case EXTENSION_POINT_INDEX_LABEL :
						extensionPoint =
							(ExtensionPointModel) objectTable.get(in.readInt());
						ExtensionPointModel[] extPointList =
							plugin.getDeclaredExtensionPoints();
						ExtensionPointModel[] newExtPointValues = null;
						if (extPointList == null) {
							newExtPointValues = new ExtensionPointModel[1];
							newExtPointValues[0] = extensionPoint;
						} else {
							newExtPointValues =
								new ExtensionPointModel[extPointList.length
									+ 1];
							System.arraycopy(
								extPointList,
								0,
								newExtPointValues,
								0,
								extPointList.length);
							newExtPointValues[extPointList.length] =
								extensionPoint;
						}
						plugin.setDeclaredExtensionPoints(newExtPointValues);
						extensionPoint = null;
						extPointList = newExtPointValues = null;
						break;
					case FRAGMENT_LABEL :
						PluginFragmentModel fragment =
							readPluginFragment(in, debugFlag);
						if (fragment == null) {
							// Something went wrong reading this fragment
							if (debugFlag) {
								String name = plugin.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read fragment for plugin " + name); //$NON-NLS-1$
							}
							plugin = null;
							done = true;
						} else {
							// Add this fragment to the end of the fragment list
							PluginFragmentModel[] fragmentList =
								plugin.getFragments();
							PluginFragmentModel[] newFragmentValues = null;
							if (fragmentList == null) {
								newFragmentValues = new PluginFragmentModel[1];
								newFragmentValues[0] = fragment;
							} else {
								newFragmentValues =
									new PluginFragmentModel[fragmentList.length
										+ 1];
								System.arraycopy(
									fragmentList,
									0,
									newFragmentValues,
									0,
									fragmentList.length);
								newFragmentValues[fragmentList.length] =
									fragment;
							}
							plugin.setFragments(newFragmentValues);
							fragment = null;
							fragmentList = newFragmentValues = null;
						}
						break;
					case FRAGMENT_INDEX_LABEL :
						fragment =
							(PluginFragmentModel) objectTable.get(in.readInt());
						PluginFragmentModel[] fragmentList =
							plugin.getFragments();
						PluginFragmentModel[] newFragmentValues = null;
						if (fragmentList == null) {
							newFragmentValues = new PluginFragmentModel[1];
							newFragmentValues[0] = fragment;
						} else {
							newFragmentValues =
								new PluginFragmentModel[fragmentList.length
									+ 1];
							System.arraycopy(
								fragmentList,
								0,
								newFragmentValues,
								0,
								fragmentList.length);
							newFragmentValues[fragmentList.length] = fragment;
						}
						plugin.setFragments(newFragmentValues);
						fragment = null;
						fragmentList = newFragmentValues = null;
						break;
					case PLUGIN_PARENT_LABEL :
						plugin.setRegistry(
							(PluginRegistryModel) objectTable.get(
								in.readInt()));
						break;
					case PLUGIN_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading plugin " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						plugin = null;
						done = true;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return plugin;
	}
	public PluginFragmentModel readPluginFragment(
		DataInputStream in,
		boolean debugFlag) {
		PluginFragmentModel fragment = cacheFactory.createPluginFragment();
		addToObjectTable(fragment);
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// plugin.
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						fragment.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						fragment.setName(in.readUTF());
						break;
					case ID_LABEL :
						fragment.setId(in.readUTF());
						break;
					case PLUGIN_PROVIDER_NAME_LABEL :
						fragment.setProviderName(in.readUTF());
						break;
					case VERSION_LABEL :
						fragment.setVersion(in.readUTF());
						break;
					case PLUGIN_LOCATION_LABEL :
						fragment.setLocation(in.readUTF());
						break;
					case FRAGMENT_PLUGIN_LABEL :
						fragment.setPlugin(in.readUTF());
						break;
					case FRAGMENT_PLUGIN_VERSION_LABEL :
						fragment.setPluginVersion(in.readUTF());
						break;
					case FRAGMENT_PLUGIN_MATCH_LABEL :
						fragment.setMatch(in.readByte());
						break;
					case PLUGIN_REQUIRES_LABEL :
						PluginPrerequisiteModel requires =
							readPluginPrerequisite(in, debugFlag);
						if (requires == null) {
							// Something went wrong reading the prerequisite
							if (debugFlag) {
								String name = fragment.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read prerequisite for fragment " + name); //$NON-NLS-1$
							}
							done = true;
							fragment = null;
						} else {
							// Add this prerequisite to the end of the requires list
							PluginPrerequisiteModel[] requiresList =
								fragment.getRequires();
							PluginPrerequisiteModel[] newRequiresValues = null;
							if (requiresList == null) {
								newRequiresValues =
									new PluginPrerequisiteModel[1];
								newRequiresValues[0] = requires;
							} else {
								newRequiresValues =
									new PluginPrerequisiteModel[requiresList
										.length
										+ 1];
								System.arraycopy(
									requiresList,
									0,
									newRequiresValues,
									0,
									requiresList.length);
								newRequiresValues[requiresList.length] =
									requires;
							}
							fragment.setRequires(newRequiresValues);
							requires = null;
							requiresList = newRequiresValues = null;
						}
						break;
					case REQUIRES_INDEX_LABEL :
						requires =
							(PluginPrerequisiteModel) objectTable.get(
								in.readInt());
						PluginPrerequisiteModel[] requiresList =
							fragment.getRequires();
						PluginPrerequisiteModel[] newRequiresValues = null;
						if (requiresList == null) {
							newRequiresValues = new PluginPrerequisiteModel[1];
							newRequiresValues[0] = requires;
						} else {
							newRequiresValues =
								new PluginPrerequisiteModel[requiresList.length
									+ 1];
							System.arraycopy(
								requiresList,
								0,
								newRequiresValues,
								0,
								requiresList.length);
							newRequiresValues[requiresList.length] = requires;
						}
						fragment.setRequires(newRequiresValues);
						requires = null;
						requiresList = newRequiresValues = null;
						break;
					case PLUGIN_LIBRARY_LABEL :
						LibraryModel library = readLibrary(in, debugFlag);
						if (library == null) {
							// Something went wrong reading this library
							if (debugFlag) {
								String name = fragment.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read library for fragment " + name); //$NON-NLS-1$
							}
							fragment = null;
							done = true;
						} else {
							// Add this library to the end of the runtime list
							LibraryModel[] libraryList = fragment.getRuntime();
							LibraryModel[] newLibraryValues = null;
							if (libraryList == null) {
								newLibraryValues = new LibraryModel[1];
								newLibraryValues[0] = library;
							} else {
								newLibraryValues =
									new LibraryModel[libraryList.length + 1];
								System.arraycopy(
									libraryList,
									0,
									newLibraryValues,
									0,
									libraryList.length);
								newLibraryValues[libraryList.length] = library;
							}
							fragment.setRuntime(newLibraryValues);
							library = null;
							libraryList = newLibraryValues = null;
						}
						break;
					case LIBRARY_INDEX_LABEL :
						library = (LibraryModel) objectTable.get(in.readInt());
						LibraryModel[] libraryList = fragment.getRuntime();
						LibraryModel[] newLibraryValues = null;
						if (libraryList == null) {
							newLibraryValues = new LibraryModel[1];
							newLibraryValues[0] = library;
						} else {
							newLibraryValues =
								new LibraryModel[libraryList.length + 1];
							System.arraycopy(
								libraryList,
								0,
								newLibraryValues,
								0,
								libraryList.length);
							newLibraryValues[libraryList.length] = library;
						}
						fragment.setRuntime(newLibraryValues);
						library = null;
						libraryList = newLibraryValues = null;
						break;
					case PLUGIN_EXTENSION_LABEL :
						ExtensionModel extension = readExtension(in, debugFlag);
						if (extension == null) {
							// Something went wrong reading this extension
							if (debugFlag) {
								String name = fragment.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read extension for fragment " + name); //$NON-NLS-1$
							}
							fragment = null;
							done = true;
						} else {
							ExtensionModel[] extList =
								fragment.getDeclaredExtensions();
							ExtensionModel[] newExtValues = null;
							if (extList == null) {
								newExtValues = new ExtensionModel[1];
								newExtValues[0] = extension;
							} else {
								newExtValues =
									new ExtensionModel[extList.length + 1];
								System.arraycopy(
									extList,
									0,
									newExtValues,
									0,
									extList.length);
								newExtValues[extList.length] = extension;
							}
							fragment.setDeclaredExtensions(newExtValues);
							extension = null;
							extList = newExtValues = null;
						}
						break;
					case EXTENSION_INDEX_LABEL :
						extension =
							(ExtensionModel) objectTable.get(in.readInt());
						ExtensionModel[] extList =
							fragment.getDeclaredExtensions();
						ExtensionModel[] newExtValues = null;
						if (extList == null) {
							newExtValues = new ExtensionModel[1];
							newExtValues[0] = extension;
						} else {
							newExtValues =
								new ExtensionModel[extList.length + 1];
							System.arraycopy(
								extList,
								0,
								newExtValues,
								0,
								extList.length);
							newExtValues[extList.length] = extension;
						}
						fragment.setDeclaredExtensions(newExtValues);
						extension = null;
						extList = newExtValues = null;
						break;
					case PLUGIN_EXTENSION_POINT_LABEL :
						ExtensionPointModel extensionPoint =
							readExtensionPoint(in, debugFlag);
						if (extensionPoint == null) {
							// Something went wrong reading this extension point
							if (debugFlag) {
								String name = fragment.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug("Unable to read extension point for fragment " + name); //$NON-NLS-1$
							}
							fragment = null;
							done = true;
						} else {
							// Add this extension point to the end of the extension point list
							ExtensionPointModel[] extPointList =
								fragment.getDeclaredExtensionPoints();
							ExtensionPointModel[] newExtPointValues = null;
							if (extPointList == null) {
								newExtPointValues = new ExtensionPointModel[1];
								newExtPointValues[0] = extensionPoint;
							} else {
								newExtPointValues =
									new ExtensionPointModel[extPointList.length
										+ 1];
								System.arraycopy(
									extPointList,
									0,
									newExtPointValues,
									0,
									extPointList.length);
								newExtPointValues[extPointList.length] =
									extensionPoint;
							}
							fragment.setDeclaredExtensionPoints(
								newExtPointValues);
							extensionPoint = null;
							extPointList = newExtPointValues = null;
						}
						break;
					case EXTENSION_POINT_INDEX_LABEL :
						extensionPoint =
							(ExtensionPointModel) objectTable.get(in.readInt());
						ExtensionPointModel[] extPointList =
							fragment.getDeclaredExtensionPoints();
						ExtensionPointModel[] newExtPointValues = null;
						if (extPointList == null) {
							newExtPointValues = new ExtensionPointModel[1];
							newExtPointValues[0] = extensionPoint;
						} else {
							newExtPointValues =
								new ExtensionPointModel[extPointList.length
									+ 1];
							System.arraycopy(
								extPointList,
								0,
								newExtPointValues,
								0,
								extPointList.length);
							newExtPointValues[extPointList.length] =
								extensionPoint;
						}
						fragment.setDeclaredExtensionPoints(newExtPointValues);
						extensionPoint = null;
						extPointList = newExtPointValues = null;
						break;
					case PLUGIN_PARENT_LABEL :
						fragment.setRegistry(
							(PluginRegistryModel) objectTable.get(
								in.readInt()));
						break;
					case FRAGMENT_END_LABEL :
						done = true;
						break;
					default :
						// We got something unexpected
						if (debugFlag) {
							String name = fragment.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading fragment " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						fragment = null;
						done = true;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return fragment;
	}
	public PluginPrerequisiteModel readPluginPrerequisite(
		DataInputStream in,
		boolean debugFlag) {
		PluginPrerequisiteModel requires =
			cacheFactory.createPluginPrerequisite();
		addToObjectTable(requires);
		// Use this flag to determine if the read-only flag should be set.  You
		// can't set it now or you won't be able to add anything more to this
		// prerequisite.
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					done = true;
					break;
				}
				switch (inByte) {
					case READONLY_LABEL :
						in.readBoolean();
						break;
					case START_LINE :
						requires.setStartLine(in.readInt());
						break;
					case NAME_LABEL :
						requires.setName(in.readUTF());
						break;
					case VERSION_LABEL :
						requires.setVersion(in.readUTF());
						break;
					case REQUIRES_MATCH_LABEL :
						requires.setMatchByte(in.readByte());
						break;
					case REQUIRES_EXPORT_LABEL :
						requires.setExport(in.readBoolean());
						break;
					case REQUIRES_OPTIONAL_LABEL :
						requires.setOptional(in.readBoolean());
						break;
					case REQUIRES_RESOLVED_VERSION_LABEL :
						requires.setResolvedVersion(in.readUTF());
						break;
					case REQUIRES_PLUGIN_NAME_LABEL :
						requires.setPlugin(in.readUTF());
						break;
					case REQUIRES_END_LABEL :
						done = true;
						break;
					default :
						// We got something we didn't expect
						// Make this an empty prerequisite
						if (debugFlag) {
							String name = requires.getName();
							if (name == null)
								name = new String("<unknown name>"); //$NON-NLS-1$
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading prerequisite " + name); //$NON-NLS-1$ //$NON-NLS-2$
						}
						done = true;
						requires = null;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		return requires;
	}
	public PluginRegistryModel readPluginRegistry(
		DataInputStream in,
		URL[] pluginPath,
		boolean debugFlag) {

		if (!interpretHeaderInformation(in)) {
			if (debugFlag)
				debug("Cache header information out of date - ignoring cache"); //$NON-NLS-1$
			return null;
		}
		PluginRegistryModel cachedRegistry =
			cacheFactory.createPluginRegistry();
		addToObjectTable(cachedRegistry);

		boolean setReadOnlyFlag = false;
		try {
			byte inByte = 0;
			boolean done = false;
			while (!done) {
				try {
					inByte = in.readByte();
				} catch (EOFException eofe) {
					// Don't return a cached registry since this exception is unexpected. In the normal
					// case we should have hit the END_REGISTRY label.
					return null;
				}
				switch (inByte) {
					case REGISTRY_LABEL :
						break;
					case READONLY_LABEL :
						if (in.readBoolean())
							setReadOnlyFlag = true;
						break;
					case REGISTRY_RESOLVED_LABEL :
						if (in.readBoolean())
							cachedRegistry.markResolved();
						break;
					case PLUGIN_LABEL :
						PluginDescriptorModel plugin = null;
						if ((plugin = readPluginDescriptor(in, debugFlag))
							!= null) {
							cachedRegistry.addPlugin(plugin);
						} else {
							// Something went wrong reading this plugin
							// Invalidate the cache
							if (debugFlag) {
								debug("Unable to read plugin descriptor for plugin registry"); //$NON-NLS-1$
							}
							done = true;
							cachedRegistry = null;
						}
						break;
					case PLUGIN_INDEX_LABEL :
						plugin =
							(PluginDescriptorModel) objectTable.get(
								in.readInt());
						cachedRegistry.addPlugin(plugin);
						break;
					case FRAGMENT_LABEL :
						PluginFragmentModel fragment = null;
						if ((fragment = readPluginFragment(in, debugFlag))
							!= null) {
							cachedRegistry.addFragment(fragment);
						} else {
							// Something went wrong reading this fragment
							// Invalidate the cache
							if (debugFlag) {
								debug("Unable to read fragment descriptor for plugin registry"); //$NON-NLS-1$
							}
							done = true;
							cachedRegistry = null;
						}
						break;
					case FRAGMENT_INDEX_LABEL :
						fragment =
							(PluginFragmentModel) objectTable.get(in.readInt());
						cachedRegistry.addFragment(fragment);
						break;
					case REGISTRY_END_LABEL :
						done = true;
						break;
					default :
						// We got something we weren't expecting
						// Invalidate this cached registry
						if (debugFlag) {
							debug("Unexpected byte code " + decipherLabel(inByte) + " reading plugin registry"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						done = true;
						cachedRegistry = null;
						break;
				}
			}
		} catch (IOException ioe) {
			return null;
		}
		if (cachedRegistry == null)
			return null;

		if (setReadOnlyFlag) {
			// If we are finished reading this registry, we don't need to worry
			// about setting the read-only flag on other objects we might wish
			// to write to.  So, just to be safe, mark the whole thing.
			cachedRegistry.markReadOnly();
		}
		// if there are no plugins in the registry, return null instead of
		// an empty registry?
		PluginDescriptorModel[] pluginList = cachedRegistry.getPlugins();
		if ((pluginList == null) || (pluginList.length == 0)) {
			return null;
		} else {
			return cachedRegistry;
		}
	}
	private String[] getPathMembers(URL path) {
		String[] list = null;
		String protocol = path.getProtocol();
		if (protocol.equals("file")) { //$NON-NLS-1$
			list = (new File(path.getFile())).list();
		} else {
			// XXX: attempt to read URL and see if we got html dir page
		}
		return list == null ? new String[0] : list;
	}
}
