#!/usr/bin/env python3
"""
compare_turtle.py

Compare two RDF files triple-by-triple and report:
- Triples present in file1 but not in file2 ("additional in file1")
- Triples present in file2 but not in file1 ("missing in file1")

The comparison is blank-node aware (uses rdflib.compare graph isomorphism).
"""

import argparse
import sys
from rdflib import Graph
from rdflib.compare import to_isomorphic, graph_diff

def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Compare two RDF graphs and list asymmetric triple differences."
    )
    p.add_argument("file1", help="Path to first RDF file (e.g., Turtle).")
    p.add_argument("file2", help="Path to second RDF file (e.g., Turtle).")
    p.add_argument(
        "--format1",
        default="turtle",
        help="RDF parse format for file1 (default: turtle). Examples: turtle, nt, xml, json-ld, trig, n3",
    )
    p.add_argument(
        "--format2",
        default="turtle",
        help="RDF parse format for file2 (default: turtle). Examples: turtle, nt, xml, json-ld, trig, n3",
    )
    p.add_argument(
        "--limit",
        type=int,
        default=None,
        help="If set, only print up to this many triples from each difference set."
    )
    p.add_argument(
        "--quiet",
        action="store_true",
        help="Only print counts; suppress listing of triples."
    )
    return p.parse_args()

def load_graph(path: str, fmt: str) -> Graph:
    g = Graph()
    g.parse(path, format=fmt)
    return g

def triple_to_nt_line(triple, ns_manager) -> str:
    """
    Render a triple deterministically as a single N-Triples-like line.
    We avoid global serialization to keep control over ordering.
    """
    s, p, o = triple
    # Use .n3 with a namespace manager for readable CURIEs where possible.
    # Note: N-Triples strictly doesn't use CURIEs, but this format is readable.
    # If you want strict N-Triples, you could build a 1-triple graph and serialize as nt.
    return f"{s.n3(ns_manager)} {p.n3(ns_manager)} {o.n3(ns_manager)} ."

def print_graph_triples(g: Graph, header: str, limit: int | None, quiet: bool):
    count = len(g)
    print(f"\n{header} (count: {count})")
    if quiet or count == 0:
        return

    ns_manager = g.namespace_manager
    shown = 0
    for t in sorted(g, key=lambda t: (str(t[0]), str(t[1]), str(t[2]))):
        print(triple_to_nt_line(t, ns_manager))
        shown += 1
        if limit is not None and shown >= limit:
            remaining = count - shown
            if remaining > 0:
                print(f"... (+{remaining} more not shown)")
            break

def main():
    args = parse_args()

    try:
        g1 = load_graph(args.file1, args.format1)
    except Exception as e:
        print(f"Error parsing file1 '{args.file1}' as {args.format1}: {e}", file=sys.stderr)
        sys.exit(2)

    try:
        g2 = load_graph(args.file2, args.format2)
    except Exception as e:
        print(f"Error parsing file2 '{args.file2}' as {args.format2}: {e}", file=sys.stderr)
        sys.exit(2)

    # Use isomorphic graphs to be robust to blank node identifiers
    iso1 = to_isomorphic(g1)
    iso2 = to_isomorphic(g2)

    # graph_diff returns (in_both, in_first_only, in_second_only)
    in_both, in_first, in_second = graph_diff(iso1, iso2)

    # Report
    print(f"Loaded:\n - {args.file1} ({len(g1)} triples)\n - {args.file2} ({len(g2)} triples)")
    print_graph_triples(in_first, "Additional triples in file1 (present in file1, not in file2)", args.limit, args.quiet)
    print_graph_triples(in_second, "Missing triples in file1 (present in file2, not in file1)", args.limit, args.quiet)

    # Exit status: 0 if equivalent, 1 otherwise
    sys.exit(0 if (len(in_first) == 0 and len(in_second) == 0) else 1)

if __name__ == "__main__":
    main()
