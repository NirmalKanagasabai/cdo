/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.ui.widgets;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.commit.CDOCommitHistory;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfoManager;
import org.eclipse.emf.cdo.common.util.CDOCommonUtil;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.ui.shared.SharedIcons;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.view.CDOView;

import org.eclipse.net4j.util.ui.StructuredContentProvider;
import org.eclipse.net4j.util.ui.TableLabelProvider;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Eike Stepper
 * @since 4.2
 */
public class CommitHistoryComposite extends Composite
{
  private CDOCommitHistory history;

  private TableViewer tableViewer;

  private LabelProvider labelProvider;

  private Input input;

  public CommitHistoryComposite(Composite parent, int style)
  {
    super(parent, style);

    setLayout(new FillLayout(SWT.HORIZONTAL));

    tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
    tableViewer.setContentProvider(new ContentProvider());

    labelProvider = new LabelProvider();
    labelProvider.support(tableViewer);
  }

  public final CDOCommitHistory getHistory()
  {
    return history;
  }

  public final TableViewer getTableViewer()
  {
    return tableViewer;
  }

  public final Input getInput()
  {
    return input;
  }

  public final void setInput(Input input)
  {
    this.input = input;

    CDOSession session = input.getSession();
    String userID = session.getUserID();
    labelProvider.setLocalUserID(userID);

    CDOCommitInfoManager commitInfoManager = session.getCommitInfoManager();
    history = commitInfoManager.getHistory(input.getBranch());
    if (history.isEmpty())
    {
      history.loadCommitInfos(25);
    }

    tableViewer.setInput(history);
  }

  @Override
  public boolean setFocus()
  {
    return tableViewer.getTable().setFocus();
  }

  @Override
  public void dispose()
  {
    input = null;
    history = null;
    super.dispose();
  }

  protected void commitInfoChanged(CDOCommitInfo newCommitInfo)
  {
  }

  /**
   * @author Eike Stepper
   */
  public static class Input
  {
    private final CDOSession session;

    private final CDOBranch branch;

    public Input(Object object)
    {
      if (object instanceof CDOSession)
      {
        session = (CDOSession)object;
        branch = null;
        return;
      }

      if (object instanceof CDOView)
      {
        CDOView view = (CDOView)object;
        session = view.getSession();
        branch = view.getBranch();
        return;
      }

      if (object instanceof EObject)
      {
        EObject eObject = (EObject)object;
        CDOObject cdoObject = CDOUtil.getCDOObject(eObject);
        if (cdoObject != null)
        {
          CDOView view = cdoObject.cdoView();
          if (view != null)
          {
            session = view.getSession();
            branch = view.getBranch();
            return;
          }
        }
      }

      throw new IllegalStateException("Illegal input: " + object);
    }

    public Input(CDOSession session, CDOBranch branch)
    {
      this.session = session;
      this.branch = branch;
    }

    public final CDOSession getSession()
    {
      return session;
    }

    public final CDOBranch getBranch()
    {
      return branch;
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + (branch == null ? 0 : branch.hashCode());
      result = prime * result + (session == null ? 0 : session.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
      {
        return true;
      }

      if (obj == null)
      {
        return false;
      }

      if (!(obj instanceof Input))
      {
        return false;
      }

      Input other = (Input)obj;
      if (branch == null)
      {
        if (other.branch != null)
        {
          return false;
        }
      }
      else if (!branch.equals(other.branch))
      {
        return false;
      }

      if (session == null)
      {
        if (other.session != null)
        {
          return false;
        }
      }
      else if (!session.equals(other.session))
      {
        return false;
      }

      return true;
    }

    @Override
    public String toString()
    {
      String str = session.getRepositoryInfo().getName();
      if (branch != null)
      {
        str += " [" + branch.getPathName() + "]";
      }

      return str;
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class ContentProvider extends StructuredContentProvider<CDOCommitHistory>
  {
    public Object[] getElements(Object inputElement)
    {
      return ((CDOCommitHistory)inputElement).getElements();
    }

    @Override
    protected void connectInput(CDOCommitHistory history)
    {
      history.addListener(this);
    }

    @Override
    protected void disconnectInput(CDOCommitHistory history)
    {
      history.removeListener(this);
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class LabelProvider extends TableLabelProvider<CDOCommitInfo>
  {
    private static final ImageDescriptor COMMIT = SharedIcons.getDescriptor(SharedIcons.OBJ_COMMIT);

    private static final ImageDescriptor PERSON = SharedIcons.getDescriptor(SharedIcons.OBJ_PERSON);

    private static final ImageDescriptor PERSON_ME = SharedIcons.getDescriptor(SharedIcons.OBJ_PERSON_ME);

    private String localUserID;

    public LabelProvider()
    {
      addColumn(new Column<CDOCommitInfo>("Time", 160)
      {
        @Override
        public String getText(CDOCommitInfo commitInfo)
        {
          return CDOCommonUtil.formatTimeStamp(commitInfo.getTimeStamp());
        }

        @Override
        public Image getImage(CDOCommitInfo commitInfo)
        {
          return (Image)getResource(COMMIT);
        }
      });

      addColumn(new Column<CDOCommitInfo>("Comment", 200)
      {
        @Override
        public String getText(CDOCommitInfo commitInfo)
        {
          return commitInfo.getComment();
        }
      });

      addColumn(new Column<CDOCommitInfo>("User", 120)
      {
        @Override
        public String getText(CDOCommitInfo commitInfo)
        {
          return commitInfo.getUserID();
        }

        @Override
        public Image getImage(CDOCommitInfo commitInfo)
        {
          String userID = commitInfo.getUserID();
          if (userID != null)
          {
            if (userID.equals(localUserID))
            {
              return (Image)getResource(PERSON_ME);
            }

            return (Image)getResource(PERSON);
          }

          return null;
        }
      });

      addColumn(new Column<CDOCommitInfo>("Branch", 160)
      {
        @Override
        public String getText(CDOCommitInfo commitInfo)
        {
          return commitInfo.getBranch().getPathName();
        }
      });
    }

    public String getLocalUserID()
    {
      return localUserID;
    }

    public void setLocalUserID(String localUserID)
    {
      this.localUserID = localUserID;
      // fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }
  }
}