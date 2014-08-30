package njuke.njuke_host.backend;

/**
 * Represents a song (i.e. a title and an artist).
 */
public class Song implements Comparable<Song> {
    private String title;
    private String artist;
    private int voteCount;
    private boolean isVoted;
    private long timeStamp;
    private long id;

    public Song(String title, String artist,long id, int voteCount) {
        this.title = title;
        this.artist = artist;
        this.isVoted = false;
        this.voteCount = voteCount;
        this.id = id;
        timeStamp = System.currentTimeMillis();
    }

    public boolean isVoted(){ return isVoted; }
    public String getTitle(){ return title; }
    public String getArtist(){ return artist; }
    public int getVoteCount(){ return voteCount; }
    public long getId(){ return id; }

    public void toggleVoted() {
        isVoted = !isVoted;
        voteCount += isVoted ? 1 : -1;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }

    @Override
    public int compareTo(Song another) {
        if(this.voteCount == another.voteCount){ //Same amount of vote, sort on timestamp
            return this.timeStamp > another.timeStamp ? -1 : 1;
        }
        return this.voteCount - another.voteCount;
    }

    @Override
    public int hashCode() {
        // TODO: Implement a better hashCode(). Currently it calls Object.hashCode() since the songs
        // in the list (see SongListActivity) have the same hashCode otherwise.
        return title.hashCode() + artist.hashCode() + super.hashCode();
    }

    public void reset() {
        timeStamp = System.currentTimeMillis();
        isVoted = false;
        voteCount = 0;

    }

}
