#!/usr/bin/perl -w

use strict;

my %sections;
my %transforms;

while(<STDIN>) {
  my $original_name = $_;
  my $filename = $_;
  chomp $original_name;
  chomp $filename;
  unless ($_ =~ /.+\.wav/) {
    next;
  }

  $filename =~ s/\s*Stems//;
  $filename =~ s/\s*Stem//;
  $filename =~ s/\s+?/_/g;
  $filename =~ /(.+)?\.(\d*)\.wav/;
  print "1: $1\t\t2:$2\n";
  #print "2: $2\n";
  
  my $members;
  
  if (! $sections{$2}) {
    $members = [];
    $sections{$2} = $members;
  } else {
    $members = $sections{$2};
  }
  push @$members, $original_name;
  $transforms{$original_name} = "$1-A-section";
}

my $i = 1;
foreach (sort keys %sections) {
  my $section = $sections{$_};
  foreach (@$section) {
    my $filename = $_;
    my $transform = $transforms{$filename};
    $transform .= "$i.wav";
    my $cmd = "mv \"$filename\" $transform";
    print "$cmd\n";
    `$cmd`;
  }
  $i++;
}
