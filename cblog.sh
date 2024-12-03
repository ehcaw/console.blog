DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$DIR/target/console.blog-1.0-SNAPSHOT.jar"  # Updated path

case "$1" in
    -ws)
        # Run web server
        java -cp "$JAR_FILE" ryans.blog.app.webapp.BlogController
        ;;
    -c|"")
        # Run CLI (default if no args)
        java -cp "$JAR_FILE" ryans.blog.app.cli.BlogCLI
        ;;
    *)
        echo "Usage: cblog [-ws|-c]"
        echo "  -ws    Start web server"
        echo "  -c     Start CLI (default)"
        exit 1
        ;;
esac
