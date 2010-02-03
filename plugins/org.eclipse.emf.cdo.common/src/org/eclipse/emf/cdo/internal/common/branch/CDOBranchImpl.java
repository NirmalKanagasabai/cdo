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
package org.eclipse.emf.cdo.internal.common.branch;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.branch.CDOBranchVersion;
import org.eclipse.emf.cdo.spi.common.branch.CDOBranchUtil;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader.BranchInfo;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranchManager.BranchLoader.SubBranchInfo;

import java.text.MessageFormat;

/**
 * @author Eike Stepper
 */
public class CDOBranchImpl implements InternalCDOBranch
{
  public static final int ILLEGAL_BRANCH_ID = Integer.MIN_VALUE;

  private int id;

  private String name;

  private Object baseOrBranchManager;

  private CDOBranchPoint head;

  private InternalCDOBranch[] branches;

  public CDOBranchImpl(int id, String name, CDOBranchPoint base)
  {
    this.id = id;
    this.name = name;
    baseOrBranchManager = base;
    head = CDOBranchUtil.createBranchPoint(this, CDOBranchPoint.UNSPECIFIED_DATE);
  }

  public CDOBranchImpl(int id, InternalCDOBranchManager branchManager)
  {
    this.id = id;
    baseOrBranchManager = branchManager;
  }

  public boolean isMainBranch()
  {
    return false;
  }

  public InternalCDOBranchManager getBranchManager()
  {
    if (isProxy())
    {
      return (InternalCDOBranchManager)baseOrBranchManager;
    }

    CDOBranchPoint base = (CDOBranchPoint)baseOrBranchManager;
    return (InternalCDOBranchManager)base.getBranch().getBranchManager();
  }

  public int getID()
  {
    return id;
  }

  public String getName()
  {
    loadIfNeeded();
    return name;
  }

  public boolean isProxy()
  {
    return name == null;
  }

  public CDOBranchPoint getBase()
  {
    loadIfNeeded();
    return (CDOBranchPoint)baseOrBranchManager;
  }

  public CDOBranchPoint getHead()
  {
    return head;
  }

  public CDOBranchPoint getPoint(long timeStamp)
  {
    return CDOBranchUtil.createBranchPoint(this, timeStamp);
  }

  public CDOBranchVersion getVersion(int version)
  {
    return CDOBranchUtil.createBranchVersion(this, version);
  }

  public InternalCDOBranch createBranch(String name, long timeStamp)
  {
    return getBranchManager().createBranch(name, this, timeStamp);
  }

  public InternalCDOBranch createBranch(String name)
  {
    return getBranchManager().createBranch(name, this, CDOBranchPoint.UNSPECIFIED_DATE);
  }

  public synchronized InternalCDOBranch[] getBranches()
  {
    loadIfNeeded();
    if (branches == null)
    {
      InternalCDOBranchManager branchManager = getBranchManager();
      SubBranchInfo[] infos = branchManager.getBranchLoader().loadSubBranches(id);
      branches = new InternalCDOBranch[infos.length];
      for (int i = 0; i < infos.length; i++)
      {
        SubBranchInfo info = infos[i];
        branches[i] = branchManager.getBranch(info.getID(), info.getName(), info.getBaseTimeStamp(), this);
      }
    }

    return branches;
  }

  public InternalCDOBranch getBranch(String path)
  {
    while (path.endsWith(PATH_SEPARATOR))
    {
      path = path.substring(0, path.length() - PATH_SEPARATOR.length());
    }

    int sep = path.indexOf(PATH_SEPARATOR);
    if (sep == -1)
    {
      return getChild(path);
    }

    String name = path.substring(0, sep);
    InternalCDOBranch child = getChild(name);

    // Recurse
    String rest = path.substring(sep + 1);
    return child.getBranch(rest);
  }

  private InternalCDOBranch getChild(String name)
  {
    InternalCDOBranch[] branches = getBranches();
    for (InternalCDOBranch branch : branches)
    {
      if (name.equals(branch.getName()))
      {
        return branch;
      }
    }

    return null;
  }

  public BranchInfo getBranchInfo()
  {
    CDOBranchPoint base = getBase();
    return new BranchInfo(getName(), base.getBranch().getID(), base.getTimeStamp());
  }

  public void setBranchInfo(String name, InternalCDOBranch baseBranch, long baseTimeStamp)
  {
    this.name = name;
    baseOrBranchManager = CDOBranchUtil.createBranchPoint(baseBranch, baseTimeStamp);
  }

  public synchronized void addChild(InternalCDOBranch branch)
  {
    if (branches == null)
    {
      branches = new InternalCDOBranch[] { branch };
    }
    else
    {
      InternalCDOBranch[] newBranches = new InternalCDOBranch[branches.length + 1];
      System.arraycopy(branches, 0, newBranches, 0, branches.length);
      newBranches[branches.length] = branch;
      branches = newBranches;
    }
  }

  public int compareTo(CDOBranch o)
  {
    int otherID = o.getID();
    return id < otherID ? -1 : id == otherID ? 0 : 1;
  }

  @Override
  public int hashCode()
  {
    return id;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }

    if (obj instanceof CDOBranch)
    {
      CDOBranch that = (CDOBranch)obj;
      return id == that.getID();
    }

    return false;
  }

  @Override
  public String toString()
  {
    if (isProxy())
    {
      return MessageFormat.format("Branch[id={0}, PROXY]", id);
    }

    return MessageFormat.format("Branch[id={0}, name={1}]", id, name);
  }

  private synchronized void loadIfNeeded()
  {
    if (isProxy())
    {
      InternalCDOBranchManager branchManager = (InternalCDOBranchManager)baseOrBranchManager;
      BranchInfo branchInfo = branchManager.getBranchLoader().loadBranch(id);

      CDOBranch baseBranch = branchManager.getBranch(branchInfo.getBaseBranchID());
      name = branchInfo.getName();
      baseOrBranchManager = CDOBranchUtil.createBranchPoint(baseBranch, branchInfo.getBaseTimeStamp());
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class Main extends CDOBranchImpl
  {
    private InternalCDOBranchManager branchManager;

    public Main(InternalCDOBranchManager branchManager, long timeStamp)
    {
      super(MAIN_BRANCH_ID, MAIN_BRANCH_NAME, CDOBranchUtil.createBranchPoint(null, timeStamp));
      this.branchManager = branchManager;
    }

    @Override
    public boolean isMainBranch()
    {
      return true;
    }

    @Override
    public InternalCDOBranchManager getBranchManager()
    {
      return branchManager;
    }
  }
}
