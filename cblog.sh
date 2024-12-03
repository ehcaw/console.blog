DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$DIR/target/console.blog-1.0-SNAPSHOT.jar"

# Help function
show_help() {
    echo "Console.Blog - A terminal-based blogging platform"
    echo ""
    echo "Usage: cblog [flag]"
    echo ""
    echo "Flags:"
    echo "  -ws         Start web server"
    echo "  -c          Start CLI (default)"
    echo "  -h, --help  Show this help message"
    echo ""
    echo "Examples:"
    echo "  cblog           # Start CLI"
    echo "  cblog -ws       # Start web server"
    echo "  cblog -h        # Show this help"
}

case "$1" in
    -ws)
        # Run web server
        java -cp "$JAR_FILE" ryans.blog.app.webapp.BlogController
        ;;
    -c|"")
        # Run CLI (default if no args)
        java -cp "$JAR_FILE" ryans.blog.app.cli.BlogCLI
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    -b|--build)
        mvn package
        ;;
    *)
        echo "Unknown flag: $1"
        echo "Use -h or --help to see available flags"
        exit 1
        ;;
esac
