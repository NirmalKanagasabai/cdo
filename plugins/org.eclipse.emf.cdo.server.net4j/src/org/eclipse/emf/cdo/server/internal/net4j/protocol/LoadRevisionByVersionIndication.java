/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.server.internal.net4j.protocol;

import org.eclipse.emf.cdo.common.branch.CDOBranchVersion;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.io.CDODataInput;
import org.eclipse.emf.cdo.common.io.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.server.internal.net4j.bundle.OM;

import org.eclipse.net4j.util.om.trace.ContextTracer;

import java.io.IOException;

/**
 * @author Eike Stepper
 */
public class LoadRevisionByVersionIndication extends CDOReadIndication
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_PROTOCOL,
      LoadRevisionByVersionIndication.class);

  private CDOID id;

  private CDOBranchVersion branchVersion;

  private int referenceChunk;

  public LoadRevisionByVersionIndication(CDOServerProtocol protocol)
  {
    super(protocol, CDOProtocolConstants.SIGNAL_LOAD_REVISION_BY_VERSION);
  }

  @Override
  protected void indicating(CDODataInput in) throws IOException
  {
    id = in.readCDOID();
    if (TRACER.isEnabled())
    {
      TRACER.format("Read id: {0}", id); //$NON-NLS-1$
    }

    branchVersion = in.readCDOBranchVersion();
    if (TRACER.isEnabled())
    {
      TRACER.format("Read branchVersion: {0}", branchVersion); //$NON-NLS-1$
    }

    referenceChunk = in.readInt();
    if (TRACER.isEnabled())
    {
      TRACER.format("Read referenceChunk: {0}", referenceChunk); //$NON-NLS-1$
    }
  }

  @Override
  protected void responding(CDODataOutput out) throws IOException
  {
    CDORevision revision = getRepository().getRevisionManager().getRevisionByVersion(id, branchVersion, referenceChunk,
        true);
    out.writeCDORevision(revision, referenceChunk);
  }
}
