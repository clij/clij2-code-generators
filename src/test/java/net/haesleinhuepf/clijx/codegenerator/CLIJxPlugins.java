package net.haesleinhuepf.clijx.codegenerator;

import net.haesleinhuepf.clij2.plugins.AffineTransform;
import net.haesleinhuepf.clij2.plugins.Scale;
import net.haesleinhuepf.clij2.plugins.*;
import net.haesleinhuepf.clij2.plugins.Equal;
import net.haesleinhuepf.clij2.plugins.EqualConstant;
import net.haesleinhuepf.clij2.plugins.NotEqual;
import net.haesleinhuepf.clij2.plugins.NotEqualConstant;
import net.haesleinhuepf.clij2.plugins.Copy;
import net.haesleinhuepf.clij2.plugins.CopySlice;
import net.haesleinhuepf.clij2.plugins.AutomaticThreshold;
import net.haesleinhuepf.clij2.plugins.Histogram;
import net.haesleinhuepf.clij2.plugins.Threshold;
import net.haesleinhuepf.clij2.plugins.Set;
import net.haesleinhuepf.clij2.plugins.SetColumn;
import net.haesleinhuepf.clij2.plugins.SetRow;
import net.haesleinhuepf.clij2.plugins.AffineTransform2D;
import net.haesleinhuepf.clij2.plugins.AffineTransform3D;
import net.haesleinhuepf.clij2.plugins.LabelToMask;
import net.haesleinhuepf.clij2.plugins.MinimumDistanceOfTouchingNeighbors;
import net.haesleinhuepf.clij2.plugins.SpotsToPointList;
import net.haesleinhuepf.clijx.bonej.BoneJConnectedComponentsLabeling;
import net.haesleinhuepf.clijx.bonej.BoneJSkeletonize3D;
import net.haesleinhuepf.clijx.imagej.ImageJFillHoles;
import net.haesleinhuepf.clijx.imagej.ImageJGaussianBlur;
import net.haesleinhuepf.clijx.imagej.ImageJVariance;
import net.haesleinhuepf.clijx.imagej.ImageJWatershed;
import net.haesleinhuepf.clijx.imagej2.*;
import net.haesleinhuepf.clijx.imagej3dsuite.ImageJ3DSuiteConnectedComponentsLabeling;
import net.haesleinhuepf.clijx.imglib2.Imglib2ConnectedComponentsLabeling;
import net.haesleinhuepf.clijx.morpholibj.*;
import net.haesleinhuepf.clijx.plugins.*;
import net.haesleinhuepf.clijx.plugins.splitstack.AbstractSplitStack;
import net.haesleinhuepf.clijx.gui.OrganiseWindows;
import net.haesleinhuepf.clijx.io.*;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij2.plugins.DrawBox;
import net.haesleinhuepf.clij2.plugins.DrawLine;
import net.haesleinhuepf.clij2.plugins.DrawSphere;
import net.haesleinhuepf.clijx.plugins.DrawTwoValueLine;
import net.haesleinhuepf.clijx.piv.FastParticleImageVelocimetry;
import net.haesleinhuepf.clijx.piv.ParticleImageVelocimetry;
import net.haesleinhuepf.clijx.piv.ParticleImageVelocimetryTimelapse;
import net.haesleinhuepf.clijx.plugins.tenengradfusion.TenengradFusion;
import net.haesleinhuepf.clijx.registration.DeformableRegistration2D;
import net.haesleinhuepf.clijx.registration.TranslationRegistration;
import net.haesleinhuepf.clijx.registration.TranslationTimelapseRegistration;
import net.haesleinhuepf.clij2.plugins.Downsample2D;
import net.haesleinhuepf.clij2.plugins.Downsample3D;
import net.haesleinhuepf.clij2.plugins.DownsampleSliceBySliceHalfMedian;
import net.haesleinhuepf.clij2.plugins.GradientX;
import net.haesleinhuepf.clij2.plugins.GradientY;
import net.haesleinhuepf.clij2.plugins.GradientZ;
import net.haesleinhuepf.clij2.plugins.LocalThreshold;
import net.haesleinhuepf.clij2.plugins.GaussianBlur2D;
import net.haesleinhuepf.clij2.plugins.GaussianBlur3D;
import net.haesleinhuepf.clijx.plugins.BlurSliceBySlice;
import net.haesleinhuepf.clijx.simpleitk.*;
import net.haesleinhuepf.clijx.weka.*;
import net.haesleinhuepf.clijx.weka.autocontext.ApplyAutoContextWekaModel;
import net.haesleinhuepf.clijx.weka.autocontext.TrainAutoContextWekaModel;

public interface CLIJxPlugins {
    public Class[] classes = {
            Kernels.class,
            BinaryUnion.class,
            BinaryIntersection.class,
            ConnectedComponentsLabeling.class,
            CountNonZeroPixels.class,
            CrossCorrelation.class,
            DifferenceOfGaussian2D.class,
            DifferenceOfGaussian3D.class,
            Extrema.class,
            LocalExtremaBox.class,
            LocalID.class,
            MaskLabel.class,
            MeanClosestSpotDistance.class,
            MeanSquaredError.class,
            MedianZProjection.class,
            NonzeroMinimumDiamond.class,
            Paste2D.class,
            Paste3D.class,
            Presign.class,
            JaccardIndex.class,
            SorensenDiceCoefficient.class,
            StandardDeviationZProjection.class,
            StackToTiles.class,
            SubtractBackground2D.class,
            SubtractBackground3D.class,
            TopHatBox.class,
            TopHatSphere.class,
            Exponential.class,
            Logarithm.class,
            GenerateDistanceMatrix.class,
            ShortestDistances.class,
            SpotsToPointList.class,
            TransposeXY.class,
            TransposeXZ.class,
            TransposeYZ.class,
            FastParticleImageVelocimetry.class,
            ParticleImageVelocimetry.class,
            ParticleImageVelocimetryTimelapse.class,
            DeformableRegistration2D.class,
            TranslationRegistration.class,
            TranslationTimelapseRegistration.class,
            SetWhereXequalsY.class,
            LaplaceDiamond.class,
            Image2DToResultsTable.class,
            WriteValuesToPositions.class,
            GetSize.class,
            MultiplyMatrix.class,
            MatrixEqual.class,
            PowerImages.class,
            Equal.class,
            GreaterOrEqual.class,
            Greater.class,
            Smaller.class,
            SmallerOrEqual.class,
            NotEqual.class,
            ReadImageFromDisc.class,
            ReadRawImageFromDisc.class,
            PreloadFromDisc.class,
            EqualConstant.class,
            GreaterOrEqualConstant.class,
            GreaterConstant.class,
            SmallerConstant.class,
            SmallerOrEqualConstant.class,
            NotEqualConstant.class,
            DrawBox.class,
            DrawLine.class,
            DrawSphere.class,
            ReplaceIntensity.class,
            BoundingBox.class,
            MinimumOfMaskedPixels.class,
            MaximumOfMaskedPixels.class,
            MeanOfMaskedPixels.class,
            LabelToMask.class,
            NClosestPoints.class,
            GaussJordan.class,
            StatisticsOfLabelledPixels.class,
            VarianceOfAllPixels.class,
            StandardDeviationOfAllPixels.class,
            VarianceOfMaskedPixels.class,
            StandardDeviationOfMaskedPixels.class,
            ExcludeLabelsOnEdges.class,
            BinarySubtract.class,
            BinaryEdgeDetection.class,
            DistanceMap.class,
            PullAsROI.class,
            PullLabelsToROIManager.class,
            NonzeroMaximumDiamond.class,
            OnlyzeroOverwriteMaximumDiamond.class,
            OnlyzeroOverwriteMaximumBox.class,
            GenerateTouchMatrix.class,
            DetectLabelEdges.class,
            StopWatch.class,
            CountTouchingNeighbors.class,
            ReplaceIntensities.class,
            DrawTwoValueLine.class,
            AverageDistanceOfNClosestPoints.class,
            SaveAsTIF.class,
            ConnectedComponentsLabelingInplace.class,
            TouchMatrixToMesh.class,
            AutomaticThresholdInplace.class,
            DifferenceOfGaussianInplace3D.class,
            AbsoluteInplace.class,
            Resample.class,
            EqualizeMeanIntensitiesOfSlices.class,
            Watershed.class,
            ResliceRadial.class,
            ShowRGB.class,
            ShowGrey.class,
            Sobel.class,
            Absolute.class,
            LaplaceBox.class,
            BottomHatBox.class,
            BottomHatSphere.class,
            ClosingBox.class,
            ClosingDiamond.class,
            OpeningBox.class,
            OpeningDiamond.class,
            MaximumXProjection.class,
            MaximumYProjection.class,
            MaximumZProjectionBounded.class,
            MinimumZProjectionBounded.class,
            MeanZProjectionBounded.class,
            NonzeroMaximumBox.class,
            NonzeroMinimumBox.class,
            MinimumZProjectionThresholdedBounded.class,
            MeanOfPixelsAboveThreshold.class,
            OrganiseWindows.class,
            DistanceMatrixToMesh.class,
            PointIndexListToMesh.class,
            MinimumOctagon.class,
            MaximumOctagon.class,
            TopHatOctagon.class,
            AddImages.class,
            AddImagesWeighted.class,
            SubtractImages.class,
            ShowGlasbeyOnGrey.class,
            AffineTransform2D.class,
            AffineTransform3D.class,
            ApplyVectorField2D.class,
            ApplyVectorField3D.class,
            ArgMaximumZProjection.class,
            Histogram.class,
            AutomaticThreshold.class,
            Threshold.class,
            BinaryOr.class,
            BinaryAnd.class,
            BinaryXOr.class,
            BinaryNot.class,
            ErodeSphere.class,
            ErodeBox.class,
            ErodeSphereSliceBySlice.class,
            ErodeBoxSliceBySlice.class,
            DilateSphere.class,
            DilateBox.class,
            DilateSphereSliceBySlice.class,
            DilateBoxSliceBySlice.class,
            Copy.class,
            CopySlice.class,
            Crop2D.class,
            Crop3D.class,
            Set.class,
            Flip2D.class,
            Flip3D.class,
            RotateCounterClockwise.class,
            RotateClockwise.class,
            Mask.class,
            MaskStackWithPlane.class,
            MaximumZProjection.class,
            MeanZProjection.class,
            MinimumZProjection.class,
            Power.class,
            DivideImages.class,
            MaximumImages.class,
            MaximumImageAndScalar.class,
            MinimumImages.class,
            MinimumImageAndScalar.class,
            MultiplyImageAndScalar.class,
            MultiplyStackWithPlane.class,
            CountNonZeroPixels2DSphere.class,
            CountNonZeroPixelsSliceBySliceSphere.class,
            CountNonZeroVoxels3DSphere.class,
            SumZProjection.class,
            SumOfAllPixels.class,
            CenterOfMass.class,
            Invert.class,
            Downsample2D.class,
            Downsample3D.class,
            DownsampleSliceBySliceHalfMedian.class,
            LocalThreshold.class,
            GradientX.class,
            GradientY.class,
            GradientZ.class,
            MultiplyImageAndCoordinate.class,
            Mean2DBox.class,
            Mean2DSphere.class,
            Mean3DBox.class,
            Mean3DSphere.class,
            MeanSliceBySliceSphere.class,
            MeanOfAllPixels.class,
            Median2DBox.class,
            Median2DSphere.class,
            Median3DBox.class,
            Median3DSphere.class,
            MedianSliceBySliceBox.class,
            MedianSliceBySliceSphere.class,
            Maximum2DSphere.class,
            Maximum3DSphere.class,
            Maximum2DBox.class,
            Maximum3DBox.class,
            MaximumSliceBySliceSphere.class,
            Minimum2DSphere.class,
            Minimum3DSphere.class,
            Minimum2DBox.class,
            Minimum3DBox.class,
            MinimumSliceBySliceSphere.class,
            MultiplyImages.class,
            GaussianBlur2D.class,
            GaussianBlur3D.class,
            BlurSliceBySlice.class,
            ResliceBottom.class,
            ResliceTop.class,
            ResliceLeft.class,
            ResliceRight.class,
            Rotate2D.class,
            Rotate3D.class,
            Scale2D.class,
            Scale3D.class,
            Translate2D.class,
            Translate3D.class,
            Clear.class,
            ClInfo.class,
            ConvertFloat.class,
            ConvertUInt8.class,
            ConvertUInt16.class,
            Create2D.class,
            Create3D.class,
            Pull.class,
            PullBinary.class,
            Push.class,
            PushCurrentSlice.class,
            PushCurrentZStack.class,
            PushCurrentSelection.class,
            PushCurrentSliceSelection.class,
            Release.class,
            AddImageAndScalar.class,
            DetectMinimaBox.class,
            DetectMaximaBox.class,
            DetectMaximaSliceBySliceBox.class,
            DetectMinimaSliceBySliceBox.class,
            MaximumOfAllPixels.class,
            MinimumOfAllPixels.class,
            ReportMemory.class,
            AbstractSplitStack.class,
            TopHatOctagonSliceBySlice.class,
            SetColumn.class,
            SetRow.class,
            SumYProjection.class,
            AverageDistanceOfTouchingNeighbors.class,
            LabelledSpotsToPointList.class,
            LabelSpots.class,
            MinimumDistanceOfTouchingNeighbors.class,
            WriteVTKLineListToDisc.class,
            WriteXYZPointListToDisc.class,
            SetWhereXgreaterThanY.class,
            SetWhereXsmallerThanY.class,
            SetNonZeroPixelsToPixelIndex.class,
            CloseIndexGapsInLabelMap.class,
            AffineTransform.class,
            Scale.class,
            CentroidsOfLabels.class,
            SetRampX.class,
            SetRampY.class,
            SetRampZ.class,
            SubtractImageFromScalar.class,
            ThresholdDefault.class,
            ThresholdOtsu.class,
            ThresholdHuang.class,
            ThresholdIntermodes.class,
            ThresholdIsoData.class,
            ThresholdIJ_IsoData.class,
            ThresholdLi.class,
            ThresholdMaxEntropy.class,
            ThresholdMean.class,
            ThresholdMinError.class,
            ThresholdMinimum.class,
            ThresholdMoments.class,
            ThresholdPercentile.class,
            ThresholdRenyiEntropy.class,
            ThresholdShanbhag.class,
            ThresholdTriangle.class,
            ThresholdYen.class,
            ExcludeLabelsSubSurface.class,
            ExcludeLabelsOnSurface.class,
            SetPlane.class,
            TenengradFusion.class,
            ImageToStack.class,
            SumXProjection.class,
            SumImageSliceBySlice.class,
            MultiplyImageStackWithScalars.class,
            Print.class,
            VoronoiOctagon.class,
            SetImageBorders.class,
            Skeletonize.class,
            FloodFillDiamond.class,
            BinaryFillHoles.class,
            ConnectedComponentsLabelingDiamond.class,
            ConnectedComponentsLabelingBox.class,
            SetRandom.class,
            InvalidateKernelCache.class,
            EntropyBox.class,
            PushTile.class,
            PullTile.class,
            ConcatenateStacks.class,
            ResultsTableToImage2D.class,
            GetAutomaticThreshold.class,
            GetDimensions.class,
            CustomOperation.class,
            ApplyAutoContextWekaModel.class,
            TrainAutoContextWekaModel.class,
            ApplyWekaModel.class,
            ApplyWekaToTable.class,
            GenerateFeatureStack.class,
            //GenerateWekaProbabilityMaps.class,
            TrainWekaModel.class,
            TrainWekaFromTable.class,
            TrainWekaModelWithOptions.class,
            StartContinuousWebcamAcquisition.class,
            StopContinuousWebcamAcquisition.class,
            CaptureWebcamImage.class,
            ConvertRGBStackToGraySlice.class,
            PullLabelsToROIList.class,
            MeanOfTouchingNeighbors.class,
            MinimumOfTouchingNeighbors.class,
            MaximumOfTouchingNeighbors.class,
            ResultsTableColumnToImage.class,
            StatisticsOfBackgroundAndLabelledPixels.class,
            GetGPUProperties.class,
            GetSumOfAllPixels.class,
            GetSorensenDiceCoefficient.class,
            GetMinimumOfAllPixels.class,
            GetMaximumOfAllPixels.class,
            GetMeanOfAllPixels.class,
            GetJaccardIndex.class,
            GetCenterOfMass.class,
            GetBoundingBox.class,
            PushArray.class,
            PullString.class,
            PushString.class,
            MedianOfTouchingNeighbors.class,
            PushResultsTableColumn.class,
            PushResultsTable.class,
            PullToResultsTable.class,
            LabelVoronoiOctagon.class,
            TouchMatrixToAdjacencyMatrix.class,
            AdjacencyMatrixToTouchMatrix.class,
            PointlistToLabelledSpots.class,
            StatisticsOfImage.class,
            NClosestDistances.class,
            ExcludeLabels.class,
            AverageDistanceOfNFarOffPoints.class,
            StandardDeviationOfTouchingNeighbors.class,
            NeighborsOfNeighbors.class,
            GenerateParametricImage.class,
            GenerateParametricImageFromResultsTableColumn.class,
            ExcludeLabelsWithValuesOutOfRange.class,
            ExcludeLabelsWithValuesWithinRange.class,
            CombineVertically.class,
            CombineHorizontally.class,
            ReduceStack.class,
            DetectMinima2DBox.class,
            DetectMaxima2DBox.class,
            DetectMinima3DBox.class,
            DetectMaxima3DBox.class,
            DepthColorProjection.class,
            GenerateBinaryOverlapMatrix.class,
            ResliceRadialTop.class,
            Convolve.class,
            NonLocalMeans.class,
            Bilateral.class,
            UndefinedToZero.class,
            GenerateJaccardIndexMatrix.class,
            GenerateTouchCountMatrix.class,
            MinimumXProjection.class,
            MinimumYProjection.class,
            MeanXProjection.class,
            MeanYProjection.class,
            SquaredDifference.class,
            AbsoluteDifference.class,
            ReplacePixelsIfZero.class,
            VoronoiLabeling.class,
            ExtendLabelingViaVoronoi.class,
            FindMaxima.class,
            MergeTouchingLabels.class,
            AverageNeighborDistanceMap.class,
            CylinderTransform.class,
            DetectAndLabelMaxima.class,
            DrawDistanceMeshBetweenTouchingLabels.class,
            DrawMeshBetweenTouchingLabels.class,
            ExcludeLabelsOutsideSizeRange.class,
            DilateLabels.class,
            FindAndLabelMaxima.class,
            MakeIsotropic.class,
            TouchingNeighborCountMap.class,
            RigidTransform.class,
            SphereTransform.class,
            SubtractGaussianBackground.class,
            ThresholdDoG.class,
            DriftCorrectionByCenterOfMassFixation.class,
            DriftCorrectionByCentroidFixation.class,
            IntensityCorrection.class,
            IntensityCorrectionAboveThresholdOtsu.class,
            MeanIntensityMap.class,
            StandardDeviationIntensityMap.class,
            PixelCountMap.class,
            ParametricWatershed.class,
            MeanZProjectionAboveThreshold.class,
            CentroidsOfBackgroundAndLabels.class,
            SeededWatershed.class,
            PushMetaData.class,
            PopMetaData.class,
            ResetMetaData.class,
            AverageDistanceOfNClosestNeighborsMap.class,
            DrawTouchCountMeshBetweenTouchingLabels.class,
            LocalMaximumAverageDistanceOfNClosestNeighborsMap.class,
            LocalMaximumAverageNeighborDistanceMap.class,
            LocalMaximumTouchingNeighborCountMap.class,
            LocalMeanAverageDistanceOfNClosestNeighborsMap.class,
            LocalMeanAverageNeighborDistanceMap.class,
            LocalMeanTouchingNeighborCountMap.class,
            LocalMeanTouchPortionMap.class,
            LocalMedianAverageDistanceOfNClosestNeighborsMap.class,
            LocalMedianAverageNeighborDistanceMap.class,
            LocalMedianTouchingNeighborCountMap.class,
            LocalMinimumAverageDistanceOfNClosestNeighborsMap.class,
            LocalMinimumAverageNeighborDistanceMap.class,
            LocalMinimumTouchingNeighborCountMap.class,
            LocalStandardDeviationAverageDistanceOfNClosestNeighborsMap.class,
            LocalStandardDeviationAverageNeighborDistanceMap.class,
            LocalStandardDeviationTouchingNeighborCountMap.class,
            MinimumIntensityMap.class,
            MaximumIntensityMap.class,
            ExtensionRatioMap.class,
            MaximumExtensionMap.class,
            GenerateIntegerGreyValueCooccurrenceCountMatrixHalfBox.class,
            GenerateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond.class,
            GetMeanOfMaskedPixels.class,
            DivideByGaussianBackground.class,
            GenerateGreyValueCooccurrenceMatrixBox.class,
            GreyLevelAtttributeFiltering.class,
            BinaryFillHolesSliceBySlice.class,
            BinaryWekaPixelClassifier.class,
            WekaLabelClassifier.class,
            GenerateLabelFeatureImage.class,
            LabelSurface.class,
            ReduceLabelsToCentroids.class,
            MeanExtensionMap.class,
            MeanZProjectionBelowThreshold.class,
            EuclideanDistanceFromLabelCentroidMap.class,
            GammaCorrection.class,
            ZPositionOfMaximumZProjection.class,
            ZPositionProjection.class,
            ZPositionRangeProjection.class,
            VarianceSphere.class,
            StandardDeviationSphere.class,
            VarianceBox.class,
            StandardDeviationBox.class,
            Tenengrad.class,
            TenengradSliceBySlice.class,
            SobelSliceBySlice.class,
            ExtendedDepthOfFocusSobelProjection.class,
            ExtendedDepthOfFocusTenengradProjection.class,
            ExtendedDepthOfFocusVarianceProjection.class,
            DrawMeshBetweenNClosestLabels.class,
            DrawMeshBetweenProximalLabels.class,
            Cosinus.class,
            Sinus.class,
            GenerateDistanceMatrixAlongAxis.class,
            MaximumDistanceOfTouchingNeighbors.class,
            MaximumTouchingNeighborDistanceMap.class,
            MinimumTouchingNeighborDistanceMap.class,
            GenerateAngleMatrix.class,
            TouchingNeighborDistanceRangeRatioMap.class,
            VoronoiOtsuLabeling.class,
            VisualizeOutlinesOnOriginal.class,
            FlagLabelsOnEdges.class,
            MaskedVoronoiLabeling.class,
            PullToResultsTableColumn.class,
            KMeansLabelClusterer.class,
            ModeOfTouchingNeighbors.class,
            GenerateProximalNeighborsMatrix.class,
            ReadIntensitiesFromMap.class,
            MaximumOfTouchingNeighborsMap.class,
            MinimumOfTouchingNeighborsMap.class,
            MeanOfTouchingNeighborsMap.class,
            ModeOfTouchingNeighborsMap.class,
            StandardDeviationOfTouchingNeighborsMap.class,
            PointIndexListToTouchMatrix.class,
            GenerateNNearestNeighborsMatrix.class,
            MaximumOfNNearestNeighborsMap.class,
            MinimumOfNNearestNeighborsMap.class,
            MeanOfNNearestNeighborsMap.class,
            ModeOfNNearestNeighborsMap.class,
            StandardDeviationOfNNearestNeighborsMap.class,
            MaximumOfProximalNeighborsMap.class,
            MinimumOfProximalNeighborsMap.class,
            MeanOfProximalNeighborsMap.class,
            ModeOfProximalNeighborsMap.class,
            StandardDeviationOfProximalNeighborsMap.class,
            LabelOverlapCountMap.class,
            LabelProximalNeighborCountMap.class,
            ReduceLabelsToLabelEdges.class,
            OutOfIntensityRange.class,
            ErodeLabels.class,
            Similar.class,
            Different.class,
            WekaRegionalLabelClassifier.class,
            LabelMeanOfLaplacianMap.class,
            MedianZProjectionMasked.class,
            MedianTouchPortionMap.class,
            NeighborCountWithTouchPortionAboveThresholdMap.class,
            DivideScalarByImage.class,
            ReadValuesFromMap.class,
            ReadValuesFromPositions.class,
            ZPositionOfMinimumZProjection.class,
            LocalThresholdPhansalkar.class,
            LocalThresholdBernsen.class,
            LocalThresholdContrast.class,
            LocalThresholdMean.class,
            LocalThresholdMedian.class,
            LocalThresholdMidGrey.class,
            LocalThresholdNiblack.class,
            LocalThresholdSauvola.class,
            ColorDeconvolution.class,
            GreyscaleOpeningBox.class,
            GreyscaleOpeningSphere.class,
            GreyscaleClosingBox.class,
            GreyscaleClosingSphere.class,
            ProximalNeighborCountMap.class,
            SubStack.class,
            DrawMeshBetweenNNearestLabels.class,


    };

    static Class[] both() {
        Class[] all = new Class[classes.length + extensions.length];
        int i = 0;
        for (int j = 0; j < classes.length; j++, i++) {
            all[i] = classes[j];
        }
        for (int j = 0; j < extensions.length; j++, i++) {
            all[i] = extensions[j];
        }
        return all;
    }

    public Class[] extensions = {

            // extensions
            ImageJFillHoles.class,
            ImageJGaussianBlur.class,
            ImageJVariance.class,
            ImageJWatershed.class,

            ImageJ3DSuiteConnectedComponentsLabeling.class,

            Imglib2ConnectedComponentsLabeling.class,

            ImageJ2FrangiVesselness.class,
            ImageJ2GaussianBlur.class,
            ImageJ2MedianBox.class,
            ImageJ2MedianSphere.class,
            ImageJ2RichardsonLucyDeconvolution.class,
            ImageJ2Tubeness.class,

            BoneJConnectedComponentsLabeling.class,
            BoneJSkeletonize3D.class,

            ConvertToFloat.class,
            ConvertToUnsignedByte.class,
            ConvertToUnsignedShort.class,
            SimpleITKBilateral.class,
            SimpleITKBinaryFillHole.class,
            SimpleITKBinaryPruning.class,
            SimpleITKBinaryThinning.class,
            SimpleITKBinomialBlur.class,
            SimpleITKBoundedReciprocal.class,
            SimpleITKCannyEdgeDetection.class,
            SimpleITKConnectedComponent.class,
            SimpleITKConnectedComponent.class,
            SimpleITKDanielssonDistanceMap.class,
            SimpleITKDiscreteGaussian.class,
            SimpleITKFFTConvolution.class,
            SimpleITKHMaxima.class,
            SimpleITKInverseDeconvolution.class,
            SimpleITKLandweberDeconvolution.class,
            SimpleITKMedian.class,
            SimpleITKMedianProjection.class,
            SimpleITKMorphologicalWatershed.class,
            SimpleITKOtsuMultipleThresholds.class,
            SimpleITKOtsuThreshold.class,
            SimpleITKRichardsonLucyDeconvolution.class,
            SimpleITKTikhonovDeconvolution.class,
            SimpleITKWienerDeconvolution.class,
            SimpleITKZeroCrossing.class,

            SimpleITKZeroCrossingBasedEdgeDetection.class,

            MorphoLibJChamferDistanceMap.class,
            MorphoLibJClassicWatershed.class,
            MorphoLibJChamferDistanceMap.class,
            MorphoLibJFillHoles.class,
            MorphoLibJFloodFillComponentsLabeling.class,
            MorphoLibJKeepLargestRegion.class,
            MorphoLibJMarkerControlledWatershed.class,
            MorphoLibJMorphologicalSegmentationLabelBorderImage.class,
            MorphoLibJMorphologicalSegmentationLabelObjectImage.class,
            MorphoLibJRemoveBorderLabels.class,
            MorphoLibJRemoveLargestRegion.class,
            MorphoLibJExtendedMinima.class,
            MorphoLibJExtendedMaxima.class

    };

    public String blockList = ";" +
            "BinaryIntersection.binaryAnd;" +
            "BinaryUnion.binaryOr;" +
            "AffineTransform.affineTransform3D;"+
            "Scale.scale3D;"+
            "ConnectedComponentsLabelingBox.connectedComponentsLabeling;" +
            "ConnectedComponentsLabelingDiamond.connectedComponentsLabeling;" +
            "Kernels.absolute;" +
            "Kernels.addImagesWeighted;" +
            "Kernels.addImages;" +
            "Kernels.subtractImages;" +
            "Kernels.subtract;" +
            "Kernels.affineTransform;" +
            "Kernels.affineTransform2D;" +
            "Kernels.affineTransform3D;" +
            "Kernels.threshold;" +
            "Kernels.applyVectorField;" +
            "Kernels.argMaximumZProjection;" +
            "Kernels.fillHistogram;" +
            "Kernels.histogram;" +
            "Kernels.automaticThreshold;" +
            "Kernels.binaryAnd;" +
            "Kernels.binaryOr;" +
            "Kernels.binaryXOr;" +
            "Kernels.binaryNot;" +
            "Kernels.erodeBox;" +
            "Kernels.erodeSphere;" +
            "Kernels.erodeBoxSliceBySlice;" +
            "Kernels.erodeSphereSliceBySlice;" +
            "Kernels.dilateBox;" +
            "Kernels.dilateSphere;" +
            "Kernels.dilateBoxSliceBySlice;" +
            "Kernels.dilateSphereSliceBySlice;" +
            "Kernels.copy;" +
            "Kernels.crop;" +
            "Kernels.copySlice;" +
            "Kernels.set;" +
            "Kernels.flip;" +
            "Kernels.rotateLeft;" +
            "Kernels.rotateRight;" +
            "Kernels.mask;" +
            "Kernels.maskStackWithPlane;" +
            "Kernels.maximumZProjection;" +
            "Kernels.meanZProjection;" +
            "Kernels.minimumZProjection;" +
            "Kernels.power;" +
            "Kernels.tenengradWeightsSliceBySlice;" +
            "Kernels.tenengradFusion;" +
            "Kernels.divideImages;" +
            "Kernels.maximumImages;" +
            "Kernels.maximumImageAndScalar;" +
            "Kernels.minimumImages;" +
            "Kernels.minimumImageAndScalar;" +
            "Kernels.multiplyImageAndScalar;" +
            "Kernels.multiplyStackWithPlane;" +
            "Kernels.countNonZeroPixelsLocallySliceBySlice;" +
            "Kernels.countNonZeroVoxelsLocally;" +
            "Kernels.countNonZeroPixelsLocally;" +
            "Kernels.sumPixels;" +
            "Kernels.sumZProjection;" +
            "Kernels.centerOfMass;" +
            "Kernels.invert;" +
            "Kernels.downsample;" +
            "Kernels.downsampleSliceBySliceHalfMedian;" +
            "Kernels.localThreshold;" +
            "Kernels.gradientX;" +
            "Kernels.gradientY;" +
            "Kernels.gradientZ;" +
            "Kernels.multiplyImageAndCoordinate;" +
            "Kernels.meanSliceBySliceSphere;" +
            "Kernels.meanBox;" +
            "Kernels.meanSphere;" +
            "Kernels.meanIJ;" +
            "Kernels.medianBox;" +
            "Kernels.medianSphere;" +
            "Kernels.medianSliceBySliceSphere;" +
            "Kernels.medianSliceBySliceBox;" +
            "Kernels.minimumSliceBySliceSphere;" +
            "Kernels.minimumSphere;" +
            "Kernels.minimumBox;" +
            "Kernels.maximumSliceBySliceSphere;" +
            "Kernels.maximumSphere;" +
            "Kernels.maximumBox;" +
            "Kernels.minimumIJ;" +
            "Kernels.maximumIJ;" +
            "Kernels.multiplyImages;" +
            "Kernels.blur;" +
            "Kernels.blurSliceBySlice;" +
            "Kernels.resliceTop;" +
            "Kernels.resliceBottom;" +
            "Kernels.resliceLeft;" +
            "Kernels.resliceRight;" +
            "Kernels.translate2D;" +
            "Kernels.translate3D;" +
            "Kernels.translate;" +
            "Kernels.scale2D;" +
            "Kernels.scale3D;" +
            "Kernels.scale;" +
            "Kernels.rotate2D;" +
            "Kernels.rotate3D;" +
            "Kernels.radialProjection;" +
            "Kernels.maximumOfAllPixels;" +
            "Kernels.minimumOfAllPixels;" +
            "Kernels.detectMinimaBox;" +
            "Kernels.detectMinimaSliceBySliceBox;" +
            "Kernels.detectMaximaBox;" +
            "Kernels.detectMaximaSliceBySliceBox;" +
            "Kernels.addImageAndScalar;" +
            "Kernels.splitStack;";

}
