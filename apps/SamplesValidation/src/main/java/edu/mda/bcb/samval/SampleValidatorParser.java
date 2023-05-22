// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.samval;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;


/**
 *
 * @author cjacoby
 */
public class SampleValidatorParser {
    private final CommandLineParser parser;
    private final Options options;
    
    public SampleValidatorParser() {
        this.parser = new DefaultParser();
        this.options = new Options();
        Option version = Option.builder("v")
                .longOpt("version")
                .desc("Get the version of SampleValidtor.")
                .build();
        this.options.addOption(version);
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Print the usage and options descriptions")
                .build();
        this.options.addOption(help);
        Option matrix = Option.builder("m")
                .longOpt("matrix")
                .hasArg()
                .desc("The matrix_data file.")
                .required()
                .build();
        this.options.addOption(matrix);
        Option batch = Option.builder("b")
                .longOpt("batch")
                .hasArg()
                .desc("The batches file.")
                .required()
                .build();
        this.options.addOption(batch);
        Option filter = Option.builder("f")
                .longOpt("filter")
                .argName("FILTER-FILE")
                .desc("Filter non-common sample-ids from the matrix file, the batches file, or both.")
                .hasArgs()
                .numberOfArgs(2)
                .optionalArg(true)
                .build();
        this.options.addOption(filter);
        Option create = Option.builder("c")
                .longOpt("create")
                .desc("Create missing batch entries for sample-ids in matrix_data which dont already have entries in the batches file.")
                .build();
        this.options.addOption(create);
    }
    
    public CommandLine parse(String[] args) throws ParseException {
        return this.parser.parse(this.options, args, true);
    }
    
    public Options getOptions() {
        return this.options;
    }
    
}
