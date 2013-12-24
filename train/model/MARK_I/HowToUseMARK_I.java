package model.MARK_I;

import model.MARK_I.connectTypes.RegionToRegionConnect;
import model.MARK_I.connectTypes.RegionToRegionRectangleConnect;
import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;

import model.MARK_I.ColumnPosition;
import model.MARK_I.Neocortex;
import model.MARK_I.Region;
import model.MARK_I.SpatialPooler;
import model.MARK_I.VisionCell;

import model.util.JsonFileInputOutput;
import com.google.gson.Gson;
import model.theory.MemoryClassifier;
import model.theory.Memory;
import java.util.Set;
import model.theory.Idea;
import java.io.IOException;
import model.Retina;
import model.NervousSystem;
import model.LateralGeniculateNucleus;

/**
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version June 26, 2013
 */
public class HowToUseMARK_I extends junit.framework.TestCase {
    private NervousSystem nervousSystem;
    private MemoryClassifier memoryClassifier_Digits;

    private Gson gson;

    public void setUp() throws IOException {
	this.gson = new Gson();
	this.nervousSystem = this.constructConnectedNervousSystem();
	this.memoryClassifier_Digits = this.trainMemoryClassifierWithNervousSystem();
    }

    private NervousSystem constructConnectedNervousSystem() {
	// construct Neocortex with just V1
	Region rootRegionOfNeocortex = new Region("V1", 4, 4, 4, 50, 3);
	RegionToRegionConnect neocortexConnectType = new RegionToRegionRectangleConnect();
	Neocortex unconnectedNeocortex = new Neocortex(rootRegionOfNeocortex,
		neocortexConnectType);

	// construct LGN
	Region LGNRegion = new Region("LGN", 8, 8, 1, 50, 3);
	LateralGeniculateNucleus unconnectedLGN = new LateralGeniculateNucleus(
		LGNRegion);

	// construct Retina
	VisionCell[][] visionCells = new VisionCell[65][65];
	for (int x = 0; x < visionCells.length; x++) {
	    for (int y = 0; y < visionCells[0].length; y++) {
		visionCells[x][y] = new VisionCell();
	    }
	}
	Retina unconnectedRetina = new Retina(visionCells);

	// construct 1 object of NervousSystem to encapsulate all classes in
	// MARK II
	NervousSystem nervousSystem = new NervousSystem(unconnectedNeocortex,
		unconnectedLGN, unconnectedRetina);

	// connect Retina to LGN
	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	LateralGeniculateNucleus LGN = nervousSystem.getCNS().getBrain()
		.getThalamus().getLGN();

	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
	retinaToLGN.connect(retina.getVisionCells(), LGN.getRegion(), 0, 0);

	// connect LGN to V1 Region of Neocortex
	Neocortex neocortex = nervousSystem.getCNS().getBrain().getCerebrum()
		.getCerebralCortex().getNeocortex();

	RegionToRegionConnect LGNToV1 = new RegionToRegionRectangleConnect();
	LGNToV1.connect(LGN.getRegion(), neocortex.getCurrentRegion(), 0, 0);

	return nervousSystem;
    }

    private MemoryClassifier trainMemoryClassifierWithNervousSystem()
	    throws IOException {
	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	Region LGNRegion = nervousSystem.getCNS().getBrain().getThalamus()
		.getLGN().getRegion();

	// Region V1 = nervousSystem.getCNS().getBrain().getCerebrum()
	// .getCerebralCortex().getNeocortex().getCurrentRegion();

	// -------------train NervousSystem update Memory----------------
	retina.seeBMPImage("2.bmp");

	SpatialPooler spatialPooler = new SpatialPooler(LGNRegion);
	spatialPooler.setLearningState(true);
	spatialPooler.performSpatialPoolingOnRegion();
	Set<ColumnPosition> LGNNeuronActivity = spatialPooler.getActiveColumnPositions();

	assertEquals(11, LGNNeuronActivity.size());

	// save LGNRegion to be viewed
	//String regionObject = this.gson1.toJson(LGNRegion);
	//JsonFileInputOutput.saveObjectToTextFile(regionObject,
	//	"./train/model/MARK_I/Region_LGN.txt");

	Idea twoIdea = new Idea("two", LGNRegion);
	twoIdea.unionColumnPositions(LGNNeuronActivity);

	Memory digitsMemory = new Memory();
	digitsMemory.addNewIdea(twoIdea);

	// TODO: train LGNStructure on many more different images of 2's

	MemoryClassifier memoryClassifier_digits = new MemoryClassifier(digitsMemory);

	// save MemoryClassifier object as a JSON file
	String memoryClassifierObject = this.gson.toJson(memoryClassifier_digits);
	JsonFileInputOutput.saveObjectToTextFile(memoryClassifierObject,
		"./train/model/MARK_I/MemoryClassifier_Digits.txt");

	return memoryClassifier_digits;
    }

    public void test_MemoryClassifierOnNewImages() throws IOException {
	String memoryClassifierAsString = JsonFileInputOutput
		.openObjectInTextFile("./train/model/MARK_I/MemoryClassifier_Digits.txt");
	MemoryClassifier mc = this.gson.fromJson(memoryClassifierAsString,
		MemoryClassifier.class);
	//System.out.println(mc.toString());

	Retina retina = nervousSystem.getPNS().getSNS().getRetina();

	Region LGNStructure = nervousSystem.getCNS().getBrain().getThalamus()
		.getLGN().getRegion();

	// retina.seeBMPImage("new2.bmp");
	// digitsSVM.updateIdeas(spatialPooler.performSpatialPoolingOnRegion());
	// digitsSVM.toString();
    }
}
