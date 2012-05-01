package org.codehaus.mojo.truezip.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.truezip.TrueZip;
import org.codehaus.mojo.truezip.TrueZipFileSet;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;

/**
 * @plexus.component role="org.codehaus.mojo.truezip.TrueZip" role-hint="default"
 */
public class DefaultTrueZip
    implements TrueZip
{

    public void sync() 
        throws FsSyncException
    {
        TVFS.umount();
    }
    
    public List<TFile> list( TrueZipFileSet fileSet, boolean verbose, Log logger )
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager( logger, verbose );
        return list( fileSet, fileSetManager );
    }

    public List<TFile> list( TrueZipFileSet fileSet )
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager();
        return list( fileSet, fileSetManager );
    }

    private List<TFile> list( TrueZipFileSet fileSet, TrueZipFileSetManager fileSetManager )
    {
        if ( StringUtils.isBlank( fileSet.getDirectory() ) )
        {
            fileSet.setDirectory( "." );
        }

        String[] files = fileSetManager.getIncludedFiles( fileSet );

        List<TFile> fileLists = new ArrayList<TFile>();

        for ( int i = 0; i < files.length; ++i )
        {
            TFile source = new TFile( fileSet.getDirectory(), files[i] );
            fileLists.add( source );
        }

        return fileLists;

    }

    public void move( TrueZipFileSet fileSet, boolean verbose, Log logger )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager( logger, verbose );
        move( fileSet, fileSetManager );
    }

    public void move( TrueZipFileSet fileSet )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager();
        move( fileSet, fileSetManager );
    }

    // ////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    public void copy( TrueZipFileSet fileSet, boolean verbose, Log logger )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager( logger, verbose );
        copy( fileSet, fileSetManager );
    }

    public void copy( TrueZipFileSet fileSet )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager();
        copy( fileSet, fileSetManager );
    }

    public void copy( TrueZipFileSet oneFileSet, TrueZipFileSetManager fileSetManager )
        throws IOException
    {
        if ( StringUtils.isBlank( oneFileSet.getDirectory() ) )
        {
            oneFileSet.setDirectory( "." );
        }

        String[] files = fileSetManager.getIncludedFiles( oneFileSet );

        for ( int i = 0; i < files.length; ++i )
        {
            String relativeDestPath = files[i];
            if ( !StringUtils.isBlank( oneFileSet.getOutputDirectory() ) )
            {
                relativeDestPath = oneFileSet.getOutputDirectory() + "/" + relativeDestPath;
            }
            TFile dest = new TFile( relativeDestPath );

            TFile source = new TFile( oneFileSet.getDirectory(), files[i] );

            this.copyFile( source, dest );
        }

    }

    // ////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////

    public void copyFile( TFile source, TFile dest )
        throws IOException
    {
        TFile destParent = (TFile) dest.getParentFile();

        if ( !destParent.isDirectory() )
        {
            if ( !destParent.mkdirs() )
            {
                throw new IOException( "Unable to create " + destParent );
            }
        }

        if ( source.isArchive() )
        {
            if ( dest.isArchive() && FileUtils.getExtension( dest.getPath() ).equals( FileUtils.getExtension( source.getPath() ) ) )
            {
                //we want verbatim/direct copy which is fast and it also keep the hash value intact.
                // convert source and dest to have NO associate archive type so that direct copy can happen
                source = new TFile( source.getParentFile(), source.getName(), TArchiveDetector.NULL );
                dest = new TFile( dest.getParentFile(), dest.getName(), TArchiveDetector.NULL );
                TVFS.umount();
                source.cp_rp( dest );
            }
            else
            {
                source.cp_rp( dest );
                
            }
        }
        else if ( source.isDirectory() )
        {
            source.cp_rp( dest );
        }
        else
        {
            TFile.cp_p( source, dest );
        }
    }

    public void moveFile( TFile source, TFile dest )
        throws IOException {

        TFile file = new TFile( source );

        TFile tofile = new TFile( dest );

        file.mv( tofile );
    }

    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////
    public void remove( TrueZipFileSet fileSet, boolean verbose, Log logger )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager( logger, verbose );
        remove( fileSet, fileSetManager );
    }

    public void remove( TrueZipFileSet fileSet )
        throws IOException
    {
        TrueZipFileSetManager fileSetManager = new TrueZipFileSetManager();
        remove( fileSet, fileSetManager );
    }

    private void remove( TrueZipFileSet oneFileSet, TrueZipFileSetManager fileSetManager )
        throws IOException
    {
        if ( StringUtils.isBlank( oneFileSet.getDirectory() ) )
        {
            throw new IOException( "FileSet's directory is required." );
        }

        TFile directory = new TFile( oneFileSet.getDirectory() );

        if ( !directory.isDirectory() )
        {
            throw new IOException( "FileSet's directory: " + directory + " not found." );
        }

        fileSetManager.delete( oneFileSet, true );

    }

    private void move( TrueZipFileSet fileSet, TrueZipFileSetManager fileSetManager )
        throws IOException
    {
        this.copy( fileSet, fileSetManager );
        this.remove( fileSet, fileSetManager );
    }
}