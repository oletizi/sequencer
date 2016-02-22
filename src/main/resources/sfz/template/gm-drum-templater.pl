#!/usr/bin/perl -w

use strict;
use warnings;

my %hh = (
    '42' => 1,
    '44' => 1,
    '46' => 1
);

print "<global>\n";
print "loop_mode=one_shot";

while (<STDIN>) {
    my $line = $_;
    my ($key, @name) = split( " ", $line );
    my $name = join( " ", @name );
    if ($hh{$key}) {
        $hh{$key} = $name;
    } else {
        printRegion( $key, $name );
    }
}

print "<group> // hihat group\n";
print "group=1\n";
print "off_by=1\n";

foreach (sort keys %hh) {
    my $key = $_;
    my $name = $hh{$key};
    printRegion( $key, $name );
}

sub printRegion {
    my ($key, $name) = @_;
    print "<region> // $name\n";
    print "key=$key\n";
    print "sample=\n";
}